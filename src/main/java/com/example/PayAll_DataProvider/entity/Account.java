package com.example.PayAll_DataProvider.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String accountNum;

	@Column(nullable = false, length = 60)
	private String accountName;

	@Column(nullable = false, length = 3)
	private String accountType;

	@Column(nullable = false)
	private Date issueDate;

	@Column(nullable = false)
	private Boolean isTaxBenefits;

	@Column(nullable = false)
	private Boolean isCma;

	@Column(nullable = false)
	private Boolean isStockTrans;

	@Column(nullable = false)
	private Boolean isAccountLink;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
	private AccountBasicInfo basicInfo;

	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccountTransaction> transactions = new ArrayList<>();
}
