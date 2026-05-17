package com.umanage.exam.service;

import com.umanage.common.exception.ConflictException;
import com.umanage.exam.entity.Exam;
import com.umanage.exam.entity.ExamSeatPlan;
import com.umanage.exam.entity.ExamSheet;
import com.umanage.exam.entity.SheetSymbolType;
import com.umanage.exam.repository.ExamRepository;
import com.umanage.exam.repository.ExamSeatPlanRepository;
import com.umanage.exam.repository.ExamSheetRepository;
import com.umanage.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OmrSheetGenerationServiceTest {

    @Mock
    private ExamRepository examRepository;
    @Mock
    private ExamSeatPlanRepository examSeatPlanRepository;
    @Mock
    private ExamSheetRepository examSheetRepository;

    private OmrSheetGenerationService omrSheetGenerationService;

    @BeforeEach
    void setUp() {
        omrSheetGenerationService = new OmrSheetGenerationService(
                examRepository,
                examSeatPlanRepository,
                examSheetRepository
        );
    }

    @Test
    void generateSheetsShouldCreateDeterministicUniqueSheetIds() {
        UUID examId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID studentA = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID studentB = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        var exam = new Exam();
        exam.setId(examId);
        exam.setVersion(4);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(examSeatPlanRepository.findByExamIdOrderBySeatNumberAsc(examId))
                .thenReturn(List.of(buildSeatPlan(1, "A-101-R1C1", studentA), buildSeatPlan(2, "A-101-R1C2", studentB)));
        when(examSheetRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        var generated = omrSheetGenerationService.generateSheets(examId, SheetSymbolType.BOTH);

        assertThat(generated).hasSize(2);
        assertThat(generated.get(0).getSheetId()).isEqualTo("EX11111111V4S0001UAAAAAA");
        assertThat(generated.get(1).getSheetId()).isEqualTo("EX11111111V4S0002UBBBBBB");
        assertThat(generated.get(0).getQrPayload()).contains("SID=EX11111111V4S0001UAAAAAA");
        assertThat(generated.get(0).getBarcodePayload()).isEqualTo(generated.get(0).getQrPayload());
    }

    @Test
    void generateSheetsShouldFailWhenSeatPlanIsMissing() {
        UUID examId = UUID.randomUUID();
        var exam = new Exam();
        exam.setId(examId);
        exam.setVersion(1);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(examSeatPlanRepository.findByExamIdOrderBySeatNumberAsc(examId)).thenReturn(List.of());

        assertThatThrownBy(() -> omrSheetGenerationService.generateSheets(examId, SheetSymbolType.QR))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Seat planning must be completed");
    }

    @Test
    void exportSheetPdfShouldProducePdfBytes() {
        var student = new User();
        student.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));

        var seat = new ExamSeatPlan();
        seat.setSeatLabel("A-101-R1C1");
        seat.setStudentUser(student);

        var sheet = new ExamSheet();
        sheet.setSheetId("EX11111111V1S0001UAAAAAA");
        sheet.setExamVersion(1);
        sheet.setSymbolType(SheetSymbolType.QR);
        sheet.setQrPayload("UMANAGE|SID=EX11111111V1S0001UAAAAAA");
        sheet.setExamSeatPlan(seat);

        when(examSheetRepository.findBySheetId("EX11111111V1S0001UAAAAAA")).thenReturn(Optional.of(sheet));

        var pdfBytes = omrSheetGenerationService.exportSheetPdf("EX11111111V1S0001UAAAAAA");
        var content = new String(pdfBytes, StandardCharsets.UTF_8);

        assertThat(content).startsWith("%PDF-1.4");
        assertThat(content).contains("EX11111111V1S0001UAAAAAA");
        assertThat(content).contains("UManage OMR Exam Sheet");
    }

    private ExamSeatPlan buildSeatPlan(int seatNumber, String seatLabel, UUID studentId) {
        var student = new User();
        student.setId(studentId);

        var seat = new ExamSeatPlan();
        seat.setSeatNumber(seatNumber);
        seat.setSeatLabel(seatLabel);
        seat.setStudentUser(student);
        return seat;
    }
}
