package com.bnguimgo.springbootrestserver.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TokenUtils {

    private String token;
    private String owner; //permet de savoir à qui appartient le token
    private Date tokenExpirationDate;


}
