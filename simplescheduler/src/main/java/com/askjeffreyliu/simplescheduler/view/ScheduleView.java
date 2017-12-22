package com.askjeffreyliu.simplescheduler.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import android.widget.LinearLayout;

import com.askjeffreyliu.simplescheduler.R;
import com.askjeffreyliu.simplescheduler.ScheduleConstant;
import com.askjeffreyliu.simplescheduler.listener.ClickScrollListener;
import com.askjeffreyliu.simplescheduler.listener.OnScheduleEventListener;
import com.askjeffreyliu.simplescheduler.model.Slot;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import static com.askjeffreyliu.simplescheduler.ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY;
import static com.askjeffreyliu.simplescheduler.ScheduleConstant.TYPE_AVAILABLE;
import static com.askjeffreyliu.simplescheduler.ScheduleConstant.TYPE_COMMITTED;
import static com.askjeffreyliu.simplescheduler.ScheduleConstant.TYPE_EMPTY;
import static com.askjeffreyliu.simplescheduler.ScheduleConstant.TYPE_TIME_OFF;
import static com.askjeffreyliu.simplescheduler.ScheduleConstant.TYPE_UNAVAILABLE;

/**
 * Created by jeff on 12/21/17.
 */

public class ScheduleView extends CardView implements ClickScrollListener {
    private boolean isDrawingAvailable = true;
    private LinearLayout slotsArea;
    private LinearLayout dragArea;
    private OnScheduleEventListener listener;
    private ArrayList<Slot> blocks = new ArrayList<>();
    private Slot movingBlock = null;

    public ScheduleView(Context context) {
        super(context);
        init();
    }

    public ScheduleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScheduleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.schedule_view, this);
        this.slotsArea = findViewById(R.id.slotsArea);
        this.dragArea = findViewById(R.id.dragArea);
        TouchDetectionView detectionView = findViewById(R.id.detection);
        detectionView.setListener(this);

        this.setRadius(getContext().getResources().getDimensionPixelSize(R.dimen.corner_radius));
    }

    @Override
    public void onIndexClicked(int index) {
        // find if this index is linked to a non-empty-slot
        if (blocks.size() == 0) {
            addBlockOnIndex(index);
            return;
        }

        for (int i = 0; i < blocks.size(); i++) {
            Slot block = blocks.get(i);
            if (block.contains(index)) {
                // block found, check type
                if (block.getType() == ScheduleConstant.TYPE_EMPTY) {
                    addBlockOnIndex(index);
                } else {
                    Logger.d("slot clicked");
                    if (listener != null) {
                        SlotView slotView = slotsArea.findViewById(block.getStart());
                        listener.onSlotClicked(slotView);
                    }
                }
                return;
            }
        }
    }

    public void setDrawingMode(boolean isDrawingAvailable) {
        this.isDrawingAvailable = isDrawingAvailable;
    }

    private void addBlockOnIndex(int index) {
        Logger.d("empty place click on" + index);
        ArrayList<Slot> breakDownList = createBreakDown();

        for (int i = index; i <= index + 4; i++) {
            if (i >= NUMBER_OF_30_MINS_PER_DAY) {
                break;
            }
            if (breakDownList.get(i).getType() != TYPE_EMPTY) {
                break;
            }
            breakDownList.get(i).setType(isDrawingAvailable ? TYPE_AVAILABLE : TYPE_UNAVAILABLE);
        }

        for (int i = index - 1; i >= index - 3; i--) {
            if (i < 0) {
                break;
            }
            if (breakDownList.get(i).getType() != TYPE_EMPTY) {
                break;
            }
            breakDownList.get(i).setType(isDrawingAvailable ? TYPE_AVAILABLE : TYPE_UNAVAILABLE);
        }

        blocks = breakDownList;
        updateModelAndUI();
    }

    @Override
    public void onIndexScrolled(int startIndex, int endIndex) {
//        Logger.d("scrolling on" + startIndex + " " + endIndex);

        // how big is one single slot?
        float singleSlotWidth = (float) getWidth() / ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY;

        // check if the start index is starting from empty area or not
        int type = getTypeAtIndex(startIndex);
        if (type == TYPE_EMPTY && movingBlock == null) {
//            Logger.d("scrolling from empty space and was not dragging");
            // set whatever drag view that we have in the drag area to be exactly from start to end

            Slot subSlotWithNoCommittedNorTimeOff = findSubAreaWithNoUnchangeableType(startIndex, endIndex);

            int leftIndex = Math.min(subSlotWithNoCommittedNorTimeOff.getStart(), subSlotWithNoCommittedNorTimeOff.getEnd());
            int rightIndex = Math.max(subSlotWithNoCommittedNorTimeOff.getStart(), subSlotWithNoCommittedNorTimeOff.getEnd());


            float dragViewWidth = singleSlotWidth * (rightIndex - leftIndex + 1);

            // where should the drag view x be? It should at least be a multiple of single slot width
            float leftX = leftIndex * singleSlotWidth;

            DragView dragView = new DragView(getContext(), new Slot(leftIndex, rightIndex, isDrawingAvailable ? TYPE_AVAILABLE : TYPE_UNAVAILABLE));
            dragView.setLayoutParams(new LinearLayout.LayoutParams(Math.round(dragViewWidth), LayoutParams.MATCH_PARENT));
            dragView.setX(leftX);
            dragArea.removeAllViews(); // we might be able to use existing one instead of removing all.
            dragArea.addView(dragView);
        } else if ((type == TYPE_AVAILABLE || type == TYPE_UNAVAILABLE) && movingBlock == null) {
            // here the user can do 2 things, either move the whole area or extends/shrink the size
            // determine if we are moving area or resizing
            // if the area is only movable for now
            // first we find which block we are trying to move base on start index
            Slot slot = findSlotByIndex(startIndex);
            if (slot != null) {
                // we found the slot, we should add a drag view at the same location of this slot
                // then remove this slot


                if (movingBlock == null) {
                    // save the moving block as a temp variable
                    movingBlock = new Slot(slot);
                } else { // was already moving the block,
                    Logger.d("was already moving the block");
                }


                int moveVector = endIndex - startIndex;

                int leftIndex = Math.min(movingBlock.getStart(), movingBlock.getEnd());
                int rightIndex = Math.max(movingBlock.getStart(), movingBlock.getEnd());

                int leftIndexUpdated = leftIndex + moveVector;
                int rightIndexUpdated = rightIndex + moveVector;


                // check if we would hit any stopping block like committed/time off
                if (moveVector > 0) {
                    int firstStoppingIndexGoingRight = findFirstIndexOfStoppingBlock(rightIndex, rightIndexUpdated);
                    if (firstStoppingIndexGoingRight != -1) {
                        // we hit a wall, stop before this location
                        moveVector = firstStoppingIndexGoingRight - rightIndex - 1;

                        leftIndexUpdated = leftIndex + moveVector;
                        rightIndexUpdated = rightIndex + moveVector;

                        Logger.d("we hit right wall at index " + firstStoppingIndexGoingRight);
                    }
                } else if (moveVector < 0) {
                    int firstStoppingIndexGoingLeft = findFirstIndexOfStoppingBlock(leftIndex, leftIndexUpdated);
                    if (firstStoppingIndexGoingLeft != -1) {
                        // we hit a wall, stop before this location
                        moveVector = firstStoppingIndexGoingLeft - leftIndex + 1;

                        leftIndexUpdated = leftIndex + moveVector;
                        rightIndexUpdated = rightIndex + moveVector;

                        Logger.d("we hit left wall at index " + firstStoppingIndexGoingLeft);
                    }
                }

                float dragViewWidth = singleSlotWidth * movingBlock.size();

                // where should the drag view x be? It should at least be a multiple of single slot width
                float leftX = (leftIndex + moveVector) * singleSlotWidth;

                DragView dragView = new DragView(getContext(), new Slot(leftIndexUpdated, rightIndexUpdated, movingBlock.getType()));
                dragView.setParentWidth(getWidth());
                dragView.setLayoutParams(new LinearLayout.LayoutParams(Math.round(dragViewWidth), LayoutParams.MATCH_PARENT));
                dragView.setX(leftX);
                dragArea.removeAllViews(); // we might be able to use existing one instead of removing all.
                dragArea.addView(dragView);
                dragView.updateDisplayLayout();
                delete(slot);
            }
        } else if (movingBlock != null) {
            // find the existing drag view
            DragView dragView = (DragView) dragArea.getChildAt(0);
            // just set new X

            int moveVector = endIndex - startIndex;

            int leftIndex = Math.min(movingBlock.getStart(), movingBlock.getEnd());
            int rightIndex = Math.max(movingBlock.getStart(), movingBlock.getEnd());

            int leftIndexUpdated = leftIndex + moveVector;
            int rightIndexUpdated = rightIndex + moveVector;


            // check if we would hit any stopping block like committed/time off
            if (moveVector > 0) {
                int firstStoppingIndexGoingRight = findFirstIndexOfStoppingBlock(rightIndex, rightIndexUpdated);
                if (firstStoppingIndexGoingRight != -1) {
                    // we hit a wall, stop before this location
                    moveVector = firstStoppingIndexGoingRight - rightIndex - 1;

                    leftIndexUpdated = leftIndex + moveVector;
                    rightIndexUpdated = rightIndex + moveVector;

                    Logger.d("we hit right wall at index " + firstStoppingIndexGoingRight);
                }
            } else if (moveVector < 0) {
                int firstStoppingIndexGoingLeft = findFirstIndexOfStoppingBlock(leftIndex, leftIndexUpdated);
                if (firstStoppingIndexGoingLeft != -1) {
                    // we hit a wall, stop before this location
                    moveVector = firstStoppingIndexGoingLeft - leftIndex + 1;

                    leftIndexUpdated = leftIndex + moveVector;
                    rightIndexUpdated = rightIndex + moveVector;

                    Logger.d("we hit left wall at index " + firstStoppingIndexGoingLeft);
                }
            }

            // where should the drag view x be? It should at least be a multiple of single slot width
            float leftX = (leftIndex + moveVector) * singleSlotWidth;
            dragView.setX(leftX);
            dragView.updateIndexAndText(leftIndexUpdated, rightIndexUpdated);
            dragView.updateDisplayLayout();
        }
    }

    @Override
    public void onIndexScrollEnd(int startIndex, int endIndex) {
        if (dragArea != null) {
            if (dragArea.getChildCount() == 1) {
                DragView dragView = (DragView) dragArea.getChildAt(0);
                Slot slot = dragView.getSlot();
                // add this slot

                ArrayList<Slot> breakDownList = createBreakDown();

                int virtualLeft = slot.getStart();
                int virtualRight = slot.getEnd();
                if (virtualLeft < 0) {
                    virtualLeft = 0;
                }
                if (virtualRight >= NUMBER_OF_30_MINS_PER_DAY) {
                    virtualRight = NUMBER_OF_30_MINS_PER_DAY - 1;
                }

                if (movingBlock != null) {
//                    Logger.d("on scroll end " + movingBlock.getType());
                    for (int i = virtualLeft; i <= virtualRight; i++) {
                        breakDownList.get(i).setType(movingBlock.getType());
                    }
                } else {
                    for (int i = virtualLeft; i <= virtualRight; i++) {
                        breakDownList.get(i).setType(isDrawingAvailable ? TYPE_AVAILABLE : TYPE_UNAVAILABLE);
                    }
                }

                blocks = breakDownList;
                updateModelAndUI();
            }
            dragArea.removeAllViews();
        }
        movingBlock = null;
    }

    public void delete(Slot slot) {
        slot.setType(ScheduleConstant.TYPE_EMPTY);
        updateModelAndUI();
    }

    public void setSlots(ArrayList<Slot> blocks) {
        this.blocks = blocks;
        updateModelAndUI();
    }

    public void setEventListener(OnScheduleEventListener listener) {
        this.listener = listener;
    }

    private ArrayList<Slot> createBreakDown() {
        // break down the original list to 48 pieces
        ArrayList<Slot> temp = new ArrayList<>();
        if (blocks.size() == 0) {
            for (int i = 0; i < NUMBER_OF_30_MINS_PER_DAY; i++) {
                temp.add(new Slot(i));
            }
        } else {
            for (int i = 0; i < blocks.size(); i++) {
                Slot slot = blocks.get(i);
                for (int j = 0; j < slot.size(); j++) {
                    temp.add(new Slot(i + j, slot.getType()));
                }
            }
        }

        if (temp.size() != ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY) {
            Logger.e("error on breaking down!");
        }
        return temp;
    }

    private void updateModelAndUI() {
        // find the number of Schedule block

        // break down the original list to 48 pieces
        ArrayList<Slot> temp = createBreakDown();


        int index = 0;
        ArrayList<Slot> result = new ArrayList<>();
        while (index < NUMBER_OF_30_MINS_PER_DAY) {
            // find a block, could be type
            Slot slot = getBlockStartEnd(temp, index);

            result.add(slot);

            if (slot.getEnd() == NUMBER_OF_30_MINS_PER_DAY - 1) {
                break;
            }
            index = slot.getEnd() + 1;
        }

        blocks = result;

        updateUiAccordingToModel();
    }

    private void updateUiAccordingToModel() {
        // remove all the view listener
        for (int i = 0; i < slotsArea.getChildCount(); i++) {
            SlotView slotView = (SlotView) slotsArea.getChildAt(i);
            slotView.setOnClickListener(null);
        }

        // remove the view doesn't remove the listeners
        slotsArea.removeAllViews();

        for (int i = 0; i < blocks.size(); i++) {
            Slot slot = blocks.get(i);
            SlotView blockView = new SlotView(getContext(), slot);
            blockView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, slot.size()));
            blockView.setId(slot.getStart());
            slotsArea.addView(blockView);
        }
    }

    private Slot getBlockStartEnd(ArrayList<Slot> list, int index) {
        int start = index;
        int end = index;
        int type = list.get(index).getType();
        while (start - 1 >= 0 && list.get(start - 1).getType() == type) {
            start--;
        }
        while (end + 1 < NUMBER_OF_30_MINS_PER_DAY && list.get(end + 1).getType() == type) {
            end++;
        }
        return new Slot(start, end, type);
    }


    private int getTypeAtIndex(int index) {
        for (int i = 0; i < blocks.size(); i++) {
            Slot slot = blocks.get(i);
            if (slot.contains(index)) {
                return slot.getType();
            }
        }
        return TYPE_EMPTY;
    }

    // when start sliding from an empty area, left or right, find the largest slot within start and
    // end that doesn't overlap any committed type or time off type, if overlap, stop at the non
    // overlapping end location, the return slot type doesn't matter, we only care about size
    private Slot findSubAreaWithNoUnchangeableType(int start, int end) {
        if (start < end) {
            for (int i = start; i <= end; i++) {
                // find the block/slot at index i, which type it is
                int type = getTypeAtIndex(i);
                if (type == TYPE_COMMITTED || type == TYPE_TIME_OFF) {
                    return new Slot(start, i - 1, TYPE_EMPTY);// type doesn't matter,
                }
            }
            return new Slot(start, end, TYPE_EMPTY);// type doesn't matter
        } else if (start > end) {
            for (int i = start; i >= end; i--) {
                // find the block/slot at index i, which type it is
                int type = getTypeAtIndex(i);
                if (type == TYPE_COMMITTED || type == TYPE_TIME_OFF) {
                    return new Slot(start, i + 1, TYPE_EMPTY);// type doesn't matter,
                }
            }
            return new Slot(start, end, TYPE_EMPTY);// type doesn't matter
        } else {
            return new Slot(start, end, TYPE_EMPTY);// type doesn't matter
        }
    }

    private Slot findSlotByIndex(int index) {
        for (int i = 0; i < blocks.size(); i++) {
            Slot slot = blocks.get(i);
            if (slot.contains(index)) {
                return slot;
            }
        }
        return null;
    }

    // if -1, non has been found
    private int findFirstIndexOfStoppingBlock(int start, int end) {
        if (start < end) {
            for (int i = start; i <= end; i++) {
                int type = getTypeAtIndex(i);
                if (type == TYPE_COMMITTED || type == TYPE_TIME_OFF) {
                    return i;
                }
            }
        } else if (start > end) {
            for (int i = start; i >= end; i--) {
                int type = getTypeAtIndex(i);
                if (type == TYPE_COMMITTED || type == TYPE_TIME_OFF) {
                    return i;
                }
            }
        }
        return -1;
    }
}
