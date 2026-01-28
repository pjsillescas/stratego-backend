package com.pdrosoft.matchmaking.stratego.dto;

import java.io.Serializable;

import com.pdrosoft.matchmaking.stratego.enums.Rank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardTileDTO implements Serializable {
	private static final long serialVersionUID = -5850120129376886855L;

	private Rank rank;
	private boolean isHostOwner;
}
