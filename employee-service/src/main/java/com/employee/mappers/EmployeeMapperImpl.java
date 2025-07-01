package com.employee.mappers;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeRequest;
import com.common.employee.enums.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

/**
 * implementation for mapper class between {@link EmployeeDto}, {@link EmployeeDto} and {@link
 * EmployeeRequest}.
 */
@Service
public class EmployeeMapperImpl implements EmployeeMapper {

  /** {@inheritDoc} */
  public EmployeeDto convert(@NotNull EmployeeRequest emplReq) {
    return EmployeeDto.builder()
        .firstName(emplReq.firstName())
        .lastName(emplReq.lastName())
        .middleInitial(emplReq.middleInitial())
        .status(EmployeeStatus.ACTIVE)
        .dateOfBirth(emplReq.dateOfBirth())
        .dateOfEmployment(emplReq.dateOfEmployment())
        .build();
  }
}
