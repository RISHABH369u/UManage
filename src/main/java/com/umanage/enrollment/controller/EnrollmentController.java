package com.umanage.enrollment.controller;

import com.umanage.common.api.ApiResponse;
import com.umanage.enrollment.dto.CreateEnrollmentRequest;
import com.umanage.enrollment.dto.EnrollmentDecisionRequest;
import com.umanage.enrollment.dto.EnrollmentRecordResponse;
import com.umanage.enrollment.dto.EnrollmentRequestResponse;
import com.umanage.enrollment.mapper.EnrollmentMapper;
import com.umanage.enrollment.service.EnrollmentQueryService;
import com.umanage.enrollment.service.EnrollmentWorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollment")
public class EnrollmentController {

    private final EnrollmentWorkflowService enrollmentWorkflowService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentController(EnrollmentWorkflowService enrollmentWorkflowService,
                                EnrollmentQueryService enrollmentQueryService,
                                EnrollmentMapper enrollmentMapper) {
        this.enrollmentWorkflowService = enrollmentWorkflowService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.enrollmentMapper = enrollmentMapper;
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('ENROLLMENT_REQUEST')")
    public ResponseEntity<ApiResponse<EnrollmentRequestResponse>> submitRequest(@Valid @RequestBody CreateEnrollmentRequest request,
                                                                                 HttpServletRequest httpRequest) {
        var saved = enrollmentWorkflowService.submitRequest(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(enrollmentMapper.toRequestResponse(saved)));
    }

    @GetMapping("/requests/me")
    @PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<EnrollmentRequestResponse>>> myRequestHistory() {
        var response = enrollmentQueryService.listMyRequestHistory().stream()
                .map(enrollmentMapper::toRequestResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/registrations/me")
    @PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<EnrollmentRecordResponse>>> myActiveRegistrations() {
        var response = enrollmentQueryService.listMyActiveRegistrations().stream()
                .map(enrollmentMapper::toRecordResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/requests/pending")
    @PreAuthorize("hasAuthority('ENROLLMENT_APPROVE')")
    public ResponseEntity<ApiResponse<List<EnrollmentRequestResponse>>> listPendingRequests() {
        var response = enrollmentQueryService.listPendingRequests().stream()
                .map(enrollmentMapper::toRequestResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasAuthority('ENROLLMENT_APPROVE')")
    public ResponseEntity<ApiResponse<EnrollmentRequestResponse>> approveRequest(@PathVariable UUID id,
                                                                                  @Valid @RequestBody EnrollmentDecisionRequest request,
                                                                                  HttpServletRequest httpRequest) {
        var saved = enrollmentWorkflowService.approveRequest(id, request.reason(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(enrollmentMapper.toRequestResponse(saved)));
    }

    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("hasAuthority('ENROLLMENT_APPROVE')")
    public ResponseEntity<ApiResponse<EnrollmentRequestResponse>> rejectRequest(@PathVariable UUID id,
                                                                                 @Valid @RequestBody EnrollmentDecisionRequest request,
                                                                                 HttpServletRequest httpRequest) {
        var saved = enrollmentWorkflowService.rejectRequest(id, request.reason(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(enrollmentMapper.toRequestResponse(saved)));
    }
}
