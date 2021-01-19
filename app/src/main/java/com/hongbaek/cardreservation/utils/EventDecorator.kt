package com.hongbaek.cardreservation.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import com.hongbaek.cardreservation.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*


/**
 * Decorate several days with a dot
 */
@SuppressLint("UseCompatLoadingForDrawables")
class EventDecorator(color: Int, dates: Collection<CalendarDay>?, context: Activity) : DayViewDecorator {
    private val drawable: Drawable = context.resources.getDrawable(R.drawable.event_dot, context.theme)
    private val color: Int = color
    private val dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.setSelectionDrawable(drawable)
        view.addSpan(DotSpan(5F, color)); // 날짜 밑에 점
    }

}
