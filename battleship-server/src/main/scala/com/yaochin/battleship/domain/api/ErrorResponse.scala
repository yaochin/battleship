package com.yaochin.battleship.domain.api

import io.swagger.annotations.ApiModelProperty

/**
  * Created on 2/8/17.
  */
case class ErrorResponse(@ApiModelProperty(required = true)
                         error: String,
                         @ApiModelProperty(required = true)
                         message: String)
