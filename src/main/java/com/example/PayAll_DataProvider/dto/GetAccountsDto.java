package com.example.PayAll_DataProvider.dto;

import java.util.List;

import com.example.PayAll_DataProvider.entity.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAccountsDto {
	private String rspCode; // 응답 코드
	private String rspMsg;  // 응답 메시지
	private String searchTimestamp; // 조회 타임스탬프
	private String nextPage; // 다음 페이지 기준값
	private int accountCnt; // 보유 계좌 수
	private List<AccountDto> accountList; // 계좌 목록
}
