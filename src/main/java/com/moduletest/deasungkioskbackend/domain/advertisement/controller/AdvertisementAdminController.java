package com.moduletest.deasungkioskbackend.domain.advertisement.controller;

import com.moduletest.deasungkioskbackend.common.dto.CommonResponse;
import com.moduletest.deasungkioskbackend.common.security.SecurityUtil;
import com.moduletest.deasungkioskbackend.domain.advertisement.dto.AdvertisementResponse;
import com.moduletest.deasungkioskbackend.domain.advertisement.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "[관리자] 광고", description = "광고 관리 CRUD")
@RestController
@RequestMapping("/api/v1/admin/advertisements")
@RequiredArgsConstructor
public class AdvertisementAdminController {

    private final AdvertisementService advertisementService;

    @Operation(summary = "광고 목록 조회",
        description = "MANAGER: 자기 지점 광고만 조회. ADMIN: 전체 광고 조회.")
    @GetMapping
    public CommonResponse<List<AdvertisementResponse>> findAllAdvertisements() {
        Long storeId = SecurityUtil.resolveStoreId(null);
        return CommonResponse.success(
            advertisementService.findAllAdvertisements(storeId));
    }

    @Operation(summary = "광고 상세 조회")
    @GetMapping("/{id}")
    public CommonResponse<AdvertisementResponse> findAdvertisementById(
        @PathVariable Long id) {
        return CommonResponse.success(
            advertisementService.findAdvertisementById(id));
    }

    @Operation(summary = "광고 등록",
        description = "multipart/form-data로 파일과 설정을 함께 전송. mediaType은 IMAGE 또는 VIDEO.\n\n"
            + "이미지 크롭: cropX, cropY, cropWidth, cropHeight를 함께 전송하면 "
            + "해당 영역만 잘라서 저장합니다. 미전송 시 원본 그대로 저장."
            + "MANAGER: 자기 지점에 자동 등록. ADMIN: storeId 필수 — 등록할 지점 지정."
            + "multipart/form-data로 파일과 설정을 함께 전송. mediaType은 IMAGE 또는 VIDEO."  )
        
    @PostMapping(consumes = "multipart/form-data")
    public CommonResponse<AdvertisementResponse> createAdvertisement(
        @RequestParam(required = false) Long storeId,
        @RequestParam MultipartFile file,
        @RequestParam(defaultValue = "IMAGE") String mediaType,
        @RequestParam(defaultValue = "0") int displayOrder,
        @RequestParam(defaultValue = "5") int displaySeconds,
        @RequestParam(required = false) Integer cropX,
        @RequestParam(required = false) Integer cropY,
        @RequestParam(required = false) Integer cropWidth,
        @RequestParam(required = false) Integer cropHeight) {
        return CommonResponse.success(
            advertisementService.createAdvertisement(
                storeId, file, mediaType, displayOrder, displaySeconds,
                cropX, cropY, cropWidth, cropHeight));
    }

    @Operation(summary = "광고 수정",
        description = "파일 변경 시 file 포함, 설정만 변경 시 file 없이 전송.\n\n"
            + "이미지 크롭: cropX, cropY, cropWidth, cropHeight를 함께 전송하면 "
            + "해당 영역만 잘라서 저장합니다.")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public CommonResponse<AdvertisementResponse> updateAdvertisement(
        @PathVariable Long id,
        @RequestParam(required = false) MultipartFile file,
        @RequestParam(required = false) String mediaType,
        @RequestParam(required = false) Integer displayOrder,
        @RequestParam(required = false) Integer displaySeconds,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) Integer cropX,
        @RequestParam(required = false) Integer cropY,
        @RequestParam(required = false) Integer cropWidth,
        @RequestParam(required = false) Integer cropHeight) {
        return CommonResponse.success(
            advertisementService.updateAdvertisement(
                id, file, mediaType, displayOrder, displaySeconds, active,
                cropX, cropY, cropWidth, cropHeight));
    }

    @Operation(summary = "광고 삭제")
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteAdvertisement(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
        return CommonResponse.success(null);
    }
}
