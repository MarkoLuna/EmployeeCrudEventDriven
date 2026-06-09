package com.users.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPage {
  private int pageNumber;
  private int pageSize;
  private long offset;
  private List<UserResponse> content;
}
