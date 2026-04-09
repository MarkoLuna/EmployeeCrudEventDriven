package com.employee.mappers;

import com.common.employee.dto.EmployeeDto;
import com.common.employee.dto.EmployeeInfo;
import org.springframework.stereotype.Service;

/**
 * implementation for mapper class between {@link EmployeeDto} and {@link
 * EmployeeInfo}.
 */
@Service
public class EmployeeMapperImpl implements EmployeeMapper {

  /** {@inheritDoc} */
  public EmployeeInfo convert(EmployeeDto empl) {

    if (empl == null) {
      return null;
    }

    return EmployeeInfo.builder()
            .firstName(empl.firstName())
            .lastName(empl.lastName())
            .middleInitial(empl.middleInitial())
            .status(empl.status())
            .dateOfBirth(empl.dateOfBirth())
            .dateOfEmployment(empl.dateOfEmployment())
            .build();
  }

  /** {@inheritDoc} */
  public EmployeeDto convert(EmployeeInfo empl) {

    if (empl == null) {
      return null;
    }

    return EmployeeDto.builder()
            .firstName(empl.firstName())
            .lastName(empl.lastName())
            .middleInitial(empl.middleInitial())
            .status(empl.status())
            .dateOfBirth(empl.dateOfBirth())
            .dateOfEmployment(empl.dateOfEmployment())
            .build();
  }
}
