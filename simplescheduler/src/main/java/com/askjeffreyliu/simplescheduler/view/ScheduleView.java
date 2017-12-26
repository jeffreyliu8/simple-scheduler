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
    private int boundaryLeftIndex = -1, boundaryRightIndex = -1; // for moving

    private boolean isResizing = false;
    private int resizeFixedPointIndex;

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
                        listener.onSlotClicked(slotView, ScheduleView.this);
                    }
                }
                return;
            }
        }
    }

    public void setDrawingMode(boolean isDrawingAvailable) {
        this.isDrawingAvailable = isDrawingAvailable;

        updateColor();
    }

    private void addBlockOnIndex(int index) {
//        Logger.d("empty place click on " + index);
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
        int type = getTypeAtIndex(startIndex, blocks);
        if ((type == TYPE_EMPTY
                || (type == TYPE_UNAVAILABLE && isDrawingAvailable)
                || (type == TYPE_AVAILABLE && !isDrawingAvailable))
                && movingBlock == null
                && !isResizing) {
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
        } else if ((type == TYPE_AVAILABLE || type == TYPE_UNAVAILABLE)
                && movingBlock == null
                && !isResizing) {
            // here the user can do 2 things, either move the whole area or extends/shrink the size
            // determine if we are moving area or resizing
            // if the area is only movable for now
            // first we find which block we are trying to move base on start index
            Slot slot = findSlotByIndex(startIndex);
            if (slot != null) {
                // we found the slot, we should add a drag view at the same location of this slot
                // then remove this slot

                // check if we are resizing or not
                if (checkIsResizing(slot, startIndex, endIndex, singleSlotWidth)) {
                    return;
                }

//                Logger.d("start moving on index " + startIndex + " with " + slot.getStart() + " - " + slot.getEnd());

                if (movingBlock == null) {
                    // save the moving block as a temp variable
                    movingBlock = new Slot(slot);
                }

                movingDragView(startIndex, endIndex, singleSlotWidth, true, slot);
            }
        } else if (movingBlock != null) {
            movingDragView(startIndex, endIndex, singleSlotWidth, false, null);
        } else if (isResizing) {
            resizeExistingDragView(endIndex, singleSlotWidth);
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
        isResizing = false;
        boundaryLeftIndex = -1;
        boundaryRightIndex = -1;
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
            blockView.setDrawingAvailable(isDrawingAvailable);
            blockView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, slot.size()));
            blockView.setId(slot.getStart());
            slotsArea.addView(blockView);
        }
    }

    private void updateColor() {
        for (int i = 0; i < slotsArea.getChildCount(); i++) {
            SlotView slotView = (SlotView) slotsArea.getChildAt(i);
            slotView.setDrawingAvailable(isDrawingAvailable);
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


    public static int getTypeAtIndex(int index, ArrayList<Slot> blockList) {
        for (int i = 0; i < blockList.size(); i++) {
            Slot slot = blockList.get(i);
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
                int type = getTypeAtIndex(i, blocks);
                if (type == TYPE_COMMITTED || type == TYPE_TIME_OFF) {
                    return new Slot(start, i - 1, TYPE_EMPTY);// type doesn't matter,
                }
            }
            return new Slot(start, end, TYPE_EMPTY);// type doesn't matter
        } else if (start > end) {
            for (int i = start; i >= end; i--) {
                // find the block/slot at index i, which type it is
                int type = getTypeAtIndex(i, blocks);
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
    private int findFirstIndexOfStoppingBlock(Slot slot, boolean isGoingLeft) {
        if (isGoingLeft) {
            for (int i = slot.getStart(); i >= 0; i--) {
                int type = getTypeAtIndex(i, blocks);
                if (type == TYPE_COMMITTED || type == TYPE_TIME_OFF) {
                    return i;
                }
            }
        } else {
            for (int i = slot.getEnd(); i < ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY; i++) {
                int type = getTypeAtIndex(i, blocks);
                if (type == TYPE_COMMITTED || type == TYPE_TIME_OFF) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void resizeOnStart(Slot slot, int fingerEndIndex, float singleSlotWidth) {
        delete(slot);
        DragView dragView = new DragView(getContext(), new Slot(resizeFixedPointIndex, resizeFixedPointIndex, isDrawingAvailable ? TYPE_AVAILABLE : TYPE_UNAVAILABLE));
        dragView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT));
        dragArea.removeAllViews();
        dragArea.addView(dragView);
        dragView.onScheduleDrag(resizeFixedPointIndex, fingerEndIndex, blocks, singleSlotWidth);
    }

    private void resizeExistingDragView(int fingerIndex, float singleSlotWidth) {
        DragView dragView = (DragView) dragArea.getChildAt(0);
        if (dragView != null) {
            dragView.onScheduleDrag(resizeFixedPointIndex, fingerIndex, blocks, singleSlotWidth);
        }
    }

    private boolean checkIsResizing(Slot slot, int startIndex, int endIndex, float singleSlotWidth) {
        if (slot.size() < 3) {
            return false;
        }

        int sideDragRange = 0;
        if (slot.size() >= 9) {
            sideDragRange = 2;
        } else if (slot.size() >= 6) {
            sideDragRange = 1;
        }

//        Logger.d("check drag  on index " + startIndex + " with " + slot.getStart() + " - " + slot.getEnd() + " slot range " + sideDragRange);

        if (startIndex <= slot.getStart() + sideDragRange) {
            // resizing the left size
            isResizing = true;
            resizeFixedPointIndex = slot.getEnd();
            resizeOnStart(slot, endIndex, singleSlotWidth);
            return true;
        } else if (startIndex >= slot.getEnd() - sideDragRange) {
            // resizing the right side
            isResizing = true;
            resizeFixedPointIndex = slot.getStart();
            resizeOnStart(slot, endIndex, singleSlotWidth);
            return true;
        }

        return false;
    }

    private void movingDragView(int startIndex, int endIndex, float singleSlotWidth, boolean isStarting, Slot startingSlot) {
        int moveVector = endIndex - startIndex;

        int leftIndex = Math.min(movingBlock.getStart(), movingBlock.getEnd());
        int rightIndex = Math.max(movingBlock.getStart(), movingBlock.getEnd());

        int leftIndexUpdated = leftIndex + moveVector;
        int rightIndexUpdated = rightIndex + moveVector;

        if (isStarting) {
            boundaryRightIndex = findFirstIndexOfStoppingBlock(startingSlot, false);
            boundaryLeftIndex = findFirstIndexOfStoppingBlock(startingSlot, true);
        }

        // check if we would hit any stopping block like committed/time off
        if (moveVector > 0) {
            // if there is a wall and if we are over it
            if (boundaryRightIndex != -1 && rightIndexUpdated >= boundaryRightIndex) {
                // we hit a wall, stop before this location
                moveVector = boundaryRightIndex - rightIndex - 1;

                leftIndexUpdated = leftIndex + moveVector;
                rightIndexUpdated = rightIndex + moveVector;
//                        Logger.d("we hit right wall at index " + firstStoppingIndexGoingRight);
            }
        } else if (moveVector < 0) {
            if (boundaryLeftIndex != -1 && leftIndexUpdated <= boundaryLeftIndex) {
                // we hit a wall, stop before this location
                moveVector = boundaryLeftIndex - leftIndex + 1;

                leftIndexUpdated = leftIndex + moveVector;
                rightIndexUpdated = rightIndex + moveVector;

//                        Logger.d("we hit left wall at index " + firstStoppingIndexGoingLeft);
            }
        }

        // where should the drag view x be? It should at least be a multiple of single slot width
        float leftX = (leftIndex + moveVector) * singleSlotWidth;

        DragView dragView;
        if (isStarting) {
            dragArea.removeAllViews(); // we might be able to use existing one instead of removing all.

            dragView = new DragView(getContext(), new Slot(leftIndexUpdated, rightIndexUpdated, movingBlock.getType()));
            dragView.setParentWidth(getWidth());
            float dragViewWidth = singleSlotWidth * movingBlock.size();
            dragView.setLayoutParams(new LinearLayout.LayoutParams(Math.round(dragViewWidth), LayoutParams.MATCH_PARENT));
            dragArea.addView(dragView);

            delete(startingSlot);
        } else {
            dragView = (DragView) dragArea.getChildAt(0);
        }
        dragView.setX(leftX);
        dragView.updateIndexAndText(leftIndexUpdated, rightIndexUpdated);
        dragView.updateDisplayLayout();
    }
}
