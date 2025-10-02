package org.example.assmsof3022.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;


@Getter
@Setter
public class ExamRequest {
    private Integer id;

    @NotBlank(message = "Môn thi không được để trống")
    private String subject;

    @NotNull(message = "Ngày thi không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng phải > 0")
    private Integer durationMinutes;

    private String location;
    private String type;

    @Size(max = 1000)
    private String description;

    // new - thời điểm notify (tùy chọn)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime remindAt;
}
