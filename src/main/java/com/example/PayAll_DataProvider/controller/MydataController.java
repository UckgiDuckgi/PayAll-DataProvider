package com.example.PayAll_DataProvider.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PayAll_DataProvider.dto.AccountDto;
import com.example.PayAll_DataProvider.dto.GetAccountsDto;
import com.example.PayAll_DataProvider.entity.Account;
import com.example.PayAll_DataProvider.service.MydataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class MydataController {

	private final MydataService mydataService;

	@GetMapping
	public ResponseEntity<Map<String, Object>> getAccountList(
		@RequestHeader("Authorization") String authorization,
		@RequestHeader("x-api-tran-id") String transactionId,
		@RequestHeader("x-api-type") String apiType,
		@RequestHeader("org_code") String orgCode,
		@RequestParam(required = false) String searchTimestamp,
		@RequestParam(required = false) String nextPage,
		@RequestParam int limit
	) {
		// todo Authorization과 기타 헤더 값을 활용해 필요한 로직 추가

		// userId를 1로 가정하여 데이터 조회
		Long userId = 1L;
		// 계좌 목록 조회
		GetAccountsDto accounts = mydataService.getAccounts(userId, searchTimestamp, nextPage, limit);

		// 응답 데이터 구성
		Map<String, Object> response = new HashMap<>();
		response.put("rsp_code", "0000"); // 성공 코드
		response.put("rsp_msg", "정상 처리"); // 성공 메시지
		response.put("search_timestamp", mydataService.getLastSearchTimestamp(userId)); // 최신 타임스탬프 반환
		response.put("account_cnt", accounts.getAccountList().size()); // 계좌 수
		response.put("account_list", accounts); // 계좌 목록

		return ResponseEntity.ok(response);
	}
}
