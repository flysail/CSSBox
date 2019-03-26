package org.fit.cssbox.layout;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import org.w3c.dom.Element;

import java.awt.*;
import java.util.ArrayList;

public class FlexContainerBlockBox extends BlockBox {

    public static final CSSProperty.FlexDirection FLEX_DIRECTION_ROW = CSSProperty.FlexDirection.ROW;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_ROW_REVERSE = CSSProperty.FlexDirection.ROW_REVERSE;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_COLUMN = CSSProperty.FlexDirection.COLUMN;
    public static final CSSProperty.FlexDirection FLEX_DIRECTION_COLUMN_REVERSE = CSSProperty.FlexDirection.COLUMN_REVERSE;

    public static final CSSProperty.FlexWrap FLEX_WRAP_NOWRAP = CSSProperty.FlexWrap.NOWRAP;
    public static final CSSProperty.FlexWrap FLEX_WRAP_WRAP = CSSProperty.FlexWrap.WRAP;
    public static final CSSProperty.FlexWrap FLEX_WRAP_REVERSE = CSSProperty.FlexWrap.WRAP_REVERSE;

    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_FLEX_START = CSSProperty.JustifyContent.FlexStart;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_FLEX_END = CSSProperty.JustifyContent.FlexEnd;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_CENTER = CSSProperty.JustifyContent.Center;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_SPACE_BETWEEN = CSSProperty.JustifyContent.SpaceBetween;
    public static final CSSProperty.JustifyContent JUSTIFY_CONTENT_SPACE_AROUND = CSSProperty.JustifyContent.SpaceAround;

    public static final CSSProperty.AlignContent ALIGN_CONTENT_FLEX_START = CSSProperty.AlignContent.FlexStart;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_FLEX_END = CSSProperty.AlignContent.FlexEnd;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_CENTER = CSSProperty.AlignContent.Center;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_SPACE_BETWEEN = CSSProperty.AlignContent.SpaceBetween;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_SPACE_AROUND = CSSProperty.AlignContent.SpaceAround;
    public static final CSSProperty.AlignContent ALIGN_CONTENT_STRETCH = CSSProperty.AlignContent.Stretch;

    public static final CSSProperty.AlignItems ALIGN_ITEMS_FLEX_START = CSSProperty.AlignItems.FlexStart;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_FLEX_END = CSSProperty.AlignItems.FlexEnd;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_CENTER = CSSProperty.AlignItems.Center;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_SPACE_BASELINE = CSSProperty.AlignItems.Baseline;
    public static final CSSProperty.AlignItems ALIGN_ITEMS_STRETCH = CSSProperty.AlignItems.Stretch;


    protected CSSProperty.FlexDirection flexDirection;
    protected CSSProperty.FlexWrap flexWrap;
    protected CSSProperty.JustifyContent justifyContent;
    protected CSSProperty.AlignContent alignContent;
    protected CSSProperty.AlignItems alignItems;

    protected boolean isDirectionRow;
    protected boolean isDirectionReversed;

    protected int mainSpace;
    protected int crossSpace;

    protected FlexLine firstLine;


    public FlexContainerBlockBox(Element n, Graphics2D g, VisualContext ctx) {
        super(n, g, ctx);
        typeoflayout = new FlexBoxLayoutManager(this);
        isblock = true;
        firstLine = null;
    }

    public FlexContainerBlockBox(InlineBox src) {
        super(src);
        typeoflayout = new FlexBoxLayoutManager(this);
        isblock = true;
        firstLine = null;
    }

    public void setMainSpace() {
        if (isDirectionRow) {
            mainSpace = getContent().width;
            if (mainSpace > getMaximalContentWidth())
                mainSpace = getMaximalContentWidth();
        } else {
            mainSpace = getContent().height;
        }
    }

    public void setCrossSpace() {
        if (isDirectionRow) {
            crossSpace = getContent().height;
        } else {
            crossSpace = getContent().width;
        }

    }

    @Override
    public void setStyle(NodeData s) {
        super.setStyle(s);
        loadFlexContainerStyles();
    }

    public void loadFlexContainerStyles() {
        flexDirection = style.getProperty("flex-direction");
        if (flexDirection == null) flexDirection = CSSProperty.FlexDirection.ROW;
        isDirectionRow = isDirectionRow();
        isDirectionReversed = isDirectionReversed();

        flexWrap = style.getProperty("flex-wrap");
        if (flexWrap == null) flexWrap = CSSProperty.FlexWrap.NOWRAP;

        justifyContent = style.getProperty("justify-content");
        if (justifyContent == null) justifyContent = CSSProperty.JustifyContent.FlexStart;

        alignContent = style.getProperty("align-content");
        if (alignContent == null) alignContent = CSSProperty.AlignContent.Stretch;

        alignItems = style.getProperty("align-items");
        if (alignItems == null) alignItems = CSSProperty.AlignItems.Stretch;
    }

    public boolean isDirectionRow() {
        if (flexDirection == FLEX_DIRECTION_ROW || flexDirection == FLEX_DIRECTION_ROW_REVERSE)
            return true;
        else
            return false;
    }

    public boolean isDirectionReversed() {
        if (flexDirection == FLEX_DIRECTION_ROW_REVERSE || flexDirection == FLEX_DIRECTION_COLUMN_REVERSE)
            return true;
        else
            return false;
    }

    protected void layoutItems(ArrayList<FlexItemBlockBox> list, FlexContainerBlockBox container) {
        int contw = container.getContentWidth();
        CSSDecoder dec = new CSSDecoder(container.ctx);

        ArrayList <FlexLine> lines = new ArrayList<>();
        FlexLine line = firstLine;
        if(line == null)
            line = new FlexLine(container, 0);
        lines.add(line);

        for (int i = 0; i < list.size(); i++) {
            FlexItemBlockBox Item = list.get(i);
            System.out.println(Item);
            Item.flexBasisValue = Item.setFlexBasisValue(dec, contw, container);
            Item.hypoteticalMainSize = Item.boundFlexBasisByMinAndMaxWidth(Item.flexBasisValue);

            if (container.isDirectionRow()) {
                Item.bounds.width = Item.hypoteticalMainSize - Item.content.width + Item.totalWidth();
                Item.content.width = Item.hypoteticalMainSize;

                Item.bounds.height = Item.totalHeight();
            } else {
                Item.bounds.height = Item.hypoteticalMainSize - Item.content.height + Item.totalHeight();
                Item.content.height = Item.hypoteticalMainSize;

                Item.bounds.width = Item.totalWidth();
            }

            boolean result = line.registerItem(Item);
            if(!result) {
                int sumOfLineAboveHeights = 0;

                for (int y = 0; y < lines.size(); y++) {
                    sumOfLineAboveHeights += lines.get(y).getHeight();
                }
                FlexLine newLine = new FlexLine(container, sumOfLineAboveHeights);
                lines.add(newLine);
                newLine.registerItem(Item);
                line = newLine;
            }

            System.out.println("BOUNDS\nvyska: " + Item.bounds.height);
            System.out.println("sirka: " + Item.bounds.width);
            System.out.println("CONTENT\nvyska: " + Item.content.height);
            System.out.println("sirka: " + Item.content.width);

//          System.out.println("\nflexBasisValue(unbounded): " + subbox.flexBasisValue);
//          System.out.println("hypoteticalMainSize(bounded): " + subbox.hypoteticalMainSize);
//          System.out.println("flexGrowValue: " + subbox.flexGrowValue);
//          System.out.println("flexShrinkValue: " + subbox.flexShrinkValue);
//          System.out.println("ORDER: " + subbox.flexOrderValue);
//
//          System.out.println("Containing block: " + subbox.getContainingBlockBox());
            System.out.println("----------------------------------\n");

            //zvetseni kontejneru, pokud mozno
            if (Item.bounds.y + Item.totalHeight() > crossSpace) {
                crossSpace = Item.bounds.y + Item.totalHeight();
                container.setContentHeight(crossSpace);
            }
        }

    }
}
