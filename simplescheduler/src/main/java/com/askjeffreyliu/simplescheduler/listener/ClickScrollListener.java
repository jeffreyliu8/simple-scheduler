package com.askjeffreyliu.simplescheduler.listener;

/**
 * Created by jeff on 12/21/17.
 */

public interface ClickScrollListener {
    void onIndexScrolled(int startIndex, int endIndex, boolean isStartScrolling);

    void onIndexScrollEnd(int endIndex);

    void onIndexClicked(int index);
}
