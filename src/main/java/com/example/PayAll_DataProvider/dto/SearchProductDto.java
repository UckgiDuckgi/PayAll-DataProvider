package com.example.PayAll_DataProvider.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchProductDto {
	private Long pCode;
	private String productName;
	private String productImage;
	private List<ShopInfoDto> storeList;

	@Data
	@Builder
	public static class ShopInfoDto {
		private String shopName;
		private Long price;
		private String shopUrl;
	}
}
