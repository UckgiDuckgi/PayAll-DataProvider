package com.example.PayAll_DataProvider.dto;

import lombok.Data;

@Data
public class AccountRequestDto {
	private String orgCode;
	private String accountNum;
	private String searchTimestamp;
}
