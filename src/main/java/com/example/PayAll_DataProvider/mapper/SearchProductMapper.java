package com.example.PayAll_DataProvider.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.example.PayAll_DataProvider.dto.SearchProductDto;

public class SearchProductMapper {
	public static SearchProductDto toDto(List<LowestPriceDto> lowestPriceList) {
		if (lowestPriceList.isEmpty()) {
			return null;
		}
		LowestPriceDto firstDto = lowestPriceList.get(0);
		List<SearchProductDto.ShopInfoDto> shopInfos = lowestPriceList.stream()
			.map(p -> SearchProductDto.ShopInfoDto.builder()
				.shopName(p.getShopName())
				.shopUrl(p.getShopUrl())
				.price(p.getPrice()).build())
			.collect(Collectors.toList());
		return SearchProductDto.builder()
			.pCode(firstDto.getPCode())
			.productName(firstDto.getProductName())
			.productImage(firstDto.getProductImage())
			.storeList(shopInfos)
			.build();
	}
}
