package com.gtnoo.mnemosyne.domain.model

import java.time.LocalDate

data class DateRange(
    val start: LocalDate,
    val end: LocalDate
) {
    companion object {
        fun lastDays(n: Int) = DateRange(
            start = LocalDate.now().minusDays(n.toLong()),
            end = LocalDate.now()
        )

        fun lastMonth() = lastDays(30)
        fun lastThreeMonths() = lastDays(90)
        fun lastYear() = lastDays(365)
    }
}
