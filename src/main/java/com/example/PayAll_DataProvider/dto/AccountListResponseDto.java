package com.example.PayAll_DataProvider.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountListResponseDto {
	private String rspCode; // 응답 코드
	private String rspMsg;  // 응답 메시지
	private String searchTimestamp; // 검색 타임스탬프
	private int accountCnt; // 계좌 수
	private List<AccountDto> accountList; // 계좌 목록
}
