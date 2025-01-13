package com.example.PayAll_DataProvider.dto;

import java.util.List;

import com.example.PayAll_DataProvider.entity.AccountBasicInfo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponseDto {
	private String rspCode;
	private String rspMsg;
	private String baseDate;
	private int basicCnt;
	private List<AccountBasicInfoDto> basicList;
}
