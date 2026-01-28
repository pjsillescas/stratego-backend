package com.pdrosoft.matchmaking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthDTO {
	@NotBlank(message = "username cannot be empty")
	private String username;

	@NotBlank(message = "password cannot be empty")
	private String password;
}
