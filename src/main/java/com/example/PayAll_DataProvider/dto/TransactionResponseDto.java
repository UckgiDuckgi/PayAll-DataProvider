package com.example.PayAll_DataProvider.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponseDto {
	private String rspCode;            // 응답 코드
	private String rspMsg;             // 응답 메시지
	private String nextPage;           // 다음 페이지 기준값
	private int transCnt;              // 거래내역 개수
	private List<AccountTransactionDto> transList; // 거래내역 리스트
}
