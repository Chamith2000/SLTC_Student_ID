package com.oexil.studentreg.dto.course;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BatchDTO {
    private Long id;
    private String name;
}
