package com.tumm.account.web.response;

import lombok.*;

/**
 * <h2>SuccessResponse</h2>
 *
 * @author aek
 * <p>
 * Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SuccessResponse {
    private Object data;
    private String message;
}
