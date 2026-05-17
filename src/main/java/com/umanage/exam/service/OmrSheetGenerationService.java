package com.umanage.exam.service;

import com.umanage.common.exception.ConflictException;
import com.umanage.common.exception.ResourceNotFoundException;
import com.umanage.exam.entity.ExamSheet;
import com.umanage.exam.entity.SheetSymbolType;
import com.umanage.exam.repository.ExamRepository;
import com.umanage.exam.repository.ExamSeatPlanRepository;
import com.umanage.exam.repository.ExamSheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OmrSheetGenerationService {

    private final ExamRepository examRepository;
    private final ExamSeatPlanRepository examSeatPlanRepository;
    private final ExamSheetRepository examSheetRepository;

    public OmrSheetGenerationService(ExamRepository examRepository,
                                     ExamSeatPlanRepository examSeatPlanRepository,
                                     ExamSheetRepository examSheetRepository) {
        this.examRepository = examRepository;
        this.examSeatPlanRepository = examSeatPlanRepository;
        this.examSheetRepository = examSheetRepository;
    }

    @Transactional
    public List<ExamSheet> generateSheets(UUID examId, SheetSymbolType symbolType) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        var seatPlans = examSeatPlanRepository.findByExamIdOrderBySeatNumberAsc(examId);
        if (seatPlans.isEmpty()) {
            throw new ConflictException("Seat planning must be completed before OMR sheet generation");
        }

        examSheetRepository.deleteByExamId(examId);

        var sheets = new ArrayList<ExamSheet>();
        for (var seatPlan : seatPlans) {
            var sheetId = buildSheetId(examId, exam.getVersion(), seatPlan.getSeatNumber(), seatPlan.getStudentUser().getId());
            var payload = buildSymbolPayload(sheetId, examId, exam.getVersion(), seatPlan.getSeatNumber(), seatPlan.getStudentUser().getId());

            var sheet = new ExamSheet();
            sheet.setExam(exam);
            sheet.setExamSeatPlan(seatPlan);
            sheet.setSheetId(sheetId);
            sheet.setExamVersion(exam.getVersion());
            sheet.setSymbolType(symbolType);
            sheet.setQrPayload(payload);
            sheet.setBarcodePayload(payload);
            sheet.setPdfFileName(sheetId + ".pdf");
            sheets.add(sheet);
        }

        return examSheetRepository.saveAll(sheets);
    }

    @Transactional(readOnly = true)
    public byte[] exportSheetPdf(String sheetId) {
        var sheet = examSheetRepository.findBySheetId(sheetId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam sheet not found"));

        var lines = List.of(
                "UManage OMR Exam Sheet",
                "Sheet ID: " + sheet.getSheetId(),
                "Exam Version: " + sheet.getExamVersion(),
                "Room/Seat: " + sheet.getExamSeatPlan().getSeatLabel(),
                "Student Ref: " + sheet.getExamSeatPlan().getStudentUser().getId(),
                "Symbol Type: " + sheet.getSymbolType().name(),
                "Symbol Payload: " + sheet.getQrPayload()
        );

        return MinimalPdfWriter.render(lines);
    }

    private String buildSheetId(UUID examId, int version, int seatNumber, UUID studentId) {
        var examToken = examId.toString().replace("-", "").substring(0, 8).toUpperCase();
        var studentToken = studentId.toString().replace("-", "");
        studentToken = studentToken.substring(studentToken.length() - 6).toUpperCase();
        return "EX" + examToken + "V" + version + "S" + String.format("%04d", seatNumber) + "U" + studentToken;
    }

    private String buildSymbolPayload(String sheetId, UUID examId, int version, int seatNumber, UUID studentId) {
        return "UMANAGE|SID=" + sheetId
                + "|EID=" + examId
                + "|VER=" + version
                + "|SEAT=" + seatNumber
                + "|STU=" + studentId;
    }

    private static class MinimalPdfWriter {
        private static byte[] render(List<String> lines) {
            var content = new StringBuilder();
            content.append("BT\n/F1 12 Tf\n50 780 Td\n");
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0) {
                    content.append("0 -18 Td\n");
                }
                content.append("(").append(escape(lines.get(i))).append(") Tj\n");
            }
            content.append("ET");

            var objects = List.of(
                    "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
                    "2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n",
                    "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n",
                    "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n",
                    "5 0 obj\n<< /Length " + content.toString().getBytes(StandardCharsets.UTF_8).length + " >>\nstream\n"
                            + content + "\nendstream\nendobj\n"
            );

            var pdf = new StringBuilder("%PDF-1.4\n");
            var offsets = new ArrayList<Integer>();
            offsets.add(0);
            for (var object : objects) {
                offsets.add(pdf.toString().getBytes(StandardCharsets.UTF_8).length);
                pdf.append(object);
            }
            var xrefOffset = pdf.toString().getBytes(StandardCharsets.UTF_8).length;
            pdf.append("xref\n0 6\n");
            pdf.append("0000000000 65535 f \n");
            for (int i = 1; i < offsets.size(); i++) {
                pdf.append(String.format("%010d 00000 n \n", offsets.get(i)));
            }
            pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\n");
            pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF");
            return pdf.toString().getBytes(StandardCharsets.UTF_8);
        }

        private static String escape(String value) {
            return value
                    .replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)");
        }
    }
}
