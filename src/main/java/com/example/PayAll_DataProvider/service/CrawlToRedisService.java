package com.example.PayAll_DataProvider.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.example.PayAll_DataProvider.dto.SearchProductDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface CrawlToRedisService {
	void saveProductToRedis();

	void saveToRedis() throws JsonProcessingException;

	LowestPriceDto getProduct(String pCode) throws JsonProcessingException;

	List<SearchProductDto> getSearchProducts(String query, int page, int size);

	LowestPriceDto crawlingProduct(String pCode) throws IOException;

	LowestPriceDto getProductByName(String productName) throws UnsupportedEncodingException;
}
