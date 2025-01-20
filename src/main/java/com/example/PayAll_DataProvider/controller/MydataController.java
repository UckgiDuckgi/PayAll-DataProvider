package com.example.PayAll_DataProvider.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PayAll_DataProvider.dto.AccountListResponseDto;
import com.example.PayAll_DataProvider.dto.AccountRequestDto;
import com.example.PayAll_DataProvider.dto.AccountResponseDto;
import com.example.PayAll_DataProvider.dto.GetAccountsDto;
import com.example.PayAll_DataProvider.dto.TransactionRequestDto;
import com.example.PayAll_DataProvider.dto.TransactionResponseDto;
import com.example.PayAll_DataProvider.service.MydataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class MydataController {

	private final MydataService mydataService;

	@GetMapping
	public ResponseEntity<AccountListResponseDto> getAccountList(
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
		System.out.println("accounts.getSearchTimestamp() = " + accounts.getSearchTimestamp());

		// AccountResponseDto 빌드
		AccountListResponseDto response = AccountListResponseDto.builder()
			.rspCode("0000") // 성공 코드
			.rspMsg("정상 처리") // 성공 메시지
			.searchTimestamp(accounts.getSearchTimestamp()) // 최신 검색 타임스탬프
			.accountCnt(accounts.getAccountList().size()) // 계좌 수
			.accountList(accounts.getAccountList()) // 계좌 목록
			.build();

		return ResponseEntity.ok(response);
	}

	@PostMapping("basic")
	public ResponseEntity<AccountResponseDto> getAccountBasicInfo(
		@RequestHeader("Authorization") String authorization,
		@RequestHeader("x-api-tran-id") String transactionId,
		@RequestHeader("x-api-type") String apiType,
		@RequestBody AccountRequestDto accountRequest
	) {
		AccountResponseDto response = mydataService.getAccountBasicInfo(accountRequest.getAccountNum());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/transactions")
	public ResponseEntity<TransactionResponseDto> getAccountTransactions(
		@RequestHeader("Authorization") String authorization,
		@RequestHeader("x-api-tran-id") String transactionId,
		@RequestHeader("x-api-type") String apiType,
		@RequestBody TransactionRequestDto request
	) {
		TransactionResponseDto response = mydataService.getMydataTransactions(request);
		return ResponseEntity.ok(response);
	}
}
