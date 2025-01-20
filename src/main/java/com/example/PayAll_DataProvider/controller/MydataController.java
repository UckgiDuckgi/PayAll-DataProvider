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
import com.example.PayAll_DataProvider.repository.UserRepository;
import com.example.PayAll_DataProvider.service.JwtService;
import com.example.PayAll_DataProvider.service.MydataService;
import com.github.dockerjava.api.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class MydataController {

	private final MydataService mydataService;
	private final JwtService jwtService;
	private final UserRepository userRepository;

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

		String authId = jwtService.extractAuthId(authorization.replace("Bearer ", ""));
		Long userId = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new UnauthorizedException("유효하지 않은 토큰입니다."))
			.getId();

		// 계좌 목록 조회
		GetAccountsDto accounts = mydataService.getAccounts(userId, searchTimestamp, nextPage, limit);

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
