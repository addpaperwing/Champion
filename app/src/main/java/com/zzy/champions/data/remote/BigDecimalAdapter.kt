package com.zzy.champions.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

object BigDecimalAdapter {
  @FromJson
  fun fromJson(string: String) = BigDecimal(string)

  @ToJson
  fun toJson(value: BigDecimal) = value.toString()
}