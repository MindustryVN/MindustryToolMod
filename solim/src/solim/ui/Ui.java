package solim.ui;

import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import solim.display.SolimImage;
import solim.display.Text;
import solim.input.Button;
import solim.input.IconButton;
import solim.input.SolimTextField;
import solim.layout.Card;
import solim.layout.Column;
import solim.layout.Divider;
import solim.layout.Row;
import solim.layout.Scroll;
import solim.layout.Spacer;
import solim.overlay.SolimDialog;
import solim.signal.Computed;
import solim.signal.Readable;
import solim.signal.Signal;

/**
 * Declarative UI facades for Solim.
 * Each method pushes a layout, runs the lambda, pops, and returns the layout
 * element.
 */
public final class Ui {
    private Ui() {
    }

    public static Column column(Runnable r) {
        Column col = new Column();
        ParentStack.push(col.table(), (table, child) -> {
            Cell<?> cell = table.add(child);
            cell.growX();
            if (isExpanding(child)) {
                cell.growY();
            }
            cell.row();
            return cell;
        });
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(col.table());
        return col;
    }

    public static Card card(Runnable r) {
        return card((arc.scene.ui.Button.ButtonStyle) null, r);
    }

    public static Card card(Drawable background, Runnable r) {
        Card c = new Card(background);
        ParentStack.push(c.container(), (table, child) -> {
            Cell<?> cell = table.add(child);
            cell.growX();
            if (isExpanding(child)) {
                cell.growY();
            }
            cell.row();
            return cell;
        });
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(c.cardButton());
        return c;
    }

    public static Card card(arc.scene.ui.Button.ButtonStyle style, Runnable r) {
        Card c = new Card(style);
        ParentStack.push(c.container(), (table, child) -> {
            Cell<?> cell = table.add(child);
            cell.growX();
            if (isExpanding(child)) {
                cell.growY();
            }
            cell.row();
            return cell;
        });
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(c.cardButton());
        return c;
    }

    public static Row row(Runnable r) {
        Row row = new Row();
        ParentStack.push(row.table(), (table, child) -> {
            Cell<?> cell = table.add(child);
            if (child instanceof TextField || isExpanding(child)) {
                cell.growX();
            }
            return cell;
        });
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(row.table());
        return row;
    }

    public static Table stack(Runnable r) {
        return row(r).table();
    }

    public static Table grid(int columns, Runnable r) {
        Table t = new Table();
        int[] count = new int[]{0};
        ParentStack.push(t, (table, child) -> {
            Cell<?> cell = table.add(child);
            if (++count[0] % Math.max(1, columns) == 0) {
                table.row();
            }
            return cell;
        });
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(t);
        return t;
    }

    public static Table wrap(Runnable r) {
        return row(r).table();
    }

    public static Scroll scroll(Runnable r) {
        Scroll s = new Scroll();
        ParentStack.push(s.content(), (table, child) -> {
            Cell<?> cell = table.add(child).growX();
            cell.row();
            return cell;
        });
        try {
            r.run();
        } finally {
            ParentStack.pop();
        }
        ParentStack.attachToParent(s.element());
        return s;
    }

    public static boolean isExpanding(Element child) {
        if (child == null) return false;
        if ("spacer".equals(child.name)) return true;
        if (child.fillParent) return true;
        if (child instanceof ScrollPane) return true;
        if (child instanceof Table) {
            Table t = (Table) child;
            if (t.getChildren().size > 0 && t.getChildren().first() instanceof ScrollPane) {
                return true;
            }
        }
        return false;
    }


    public static Table container(Runnable r) {
        return column(r).table();
    }

    public static Element divider() {
        Divider d = new Divider();
        ParentStack.attachToParent(d.table());
        return d.table();
    }

    public static Element spacer() {
        Spacer s = new Spacer();
        ParentStack.attachToParent(s.element());
        return s.element();
    }

    public static SolimImage.SizedImage image(Drawable drawable) {
        SolimImage.SizedImage img = new SolimImage.SizedImage(drawable);
        ParentStack.attachToParent(img);
        return img;
    }

    public static SolimImage.SizedImage icon(Drawable drawable) {
        return image(drawable);
    }

    public static Button button(String text, Runnable onClick) {
        Button b = Button.of(text, onClick);
        ParentStack.attachToParent(b.textButton());
        return b;
    }

    public static Button button(String text, Drawable icon, Runnable onClick) {
        Button b = Button.of(text, onClick);
        if (icon != null) {
            b.textButton().add(new Image(icon)).size(24f).padRight(6f);
        }
        ParentStack.attachToParent(b.textButton());
        return b;
    }

    public static Button button(Signal<String> text, Runnable onClick) {
        Button b = Button.of(text, onClick);
        ParentStack.attachToParent(b.textButton());
        return b;
    }

    public static Button button(Computed<String> text, Runnable onClick) {
        Button b = Button.of(text, onClick);
        ParentStack.attachToParent(b.textButton());
        return b;
    }

    public static IconButton iconButton(Drawable icon, Runnable onClick) {
        IconButton b = IconButton.of(icon, onClick);
        ParentStack.attachToParent(b.imageButton());
        return b;
    }

    public static IconButton iconButton(Drawable icon, arc.scene.ui.ImageButton.ImageButtonStyle style, Runnable onClick) {
        IconButton b = IconButton.of(icon, style, onClick);
        ParentStack.attachToParent(b.imageButton());
        return b;
    }

    public static Text text(String s) {
        Text t = Text.of(s);
        ParentStack.attachToParent(t.label());
        return t;
    }

    public static Text text(Signal<String> s) {
        Text t = Text.of(s);
        ParentStack.attachToParent(t.label());
        return t;
    }

    public static Text text(Computed<String> s) {
        Text t = Text.of(s);
        ParentStack.attachToParent(t.label());
        return t;
    }

    public static Text text(Readable<String> s) {
        Text t = Text.of(s);
        ParentStack.attachToParent(t.label());
        return t;
    }

    public static SolimTextField textField(Signal<String> signal) {
        SolimTextField tf = SolimTextField.of(signal);
        ParentStack.attachToParent(tf.field());
        return tf;
    }

    public static SolimDialog dialog(String title, Runnable content) {
        return SolimDialog.of(title, content);
    }

    public static <T> Dynamic<T> dynamic(solim.signal.Readable<T> source,
            java.util.function.Function<T, solim.core.Component> factory) {
        Dynamic<T> d = Dynamic.of(source, factory);
        ParentStack.attachToParent(d.element());
        return d;
    }

    public static <T, K> ForEach<T, K> forEach(
            solim.signal.Readable<? extends Iterable<T>> collection,
            java.util.function.Function<T, K> keyExtractor,
            java.util.function.Function<T, solim.core.Component> itemFactory) {
        ForEach<T, K> fe = ForEach.of(collection, keyExtractor, itemFactory);
        ParentStack.attachToParent(fe.element());
        return fe;
    }

    public static <T, K> solim.layout.ReactiveGrid<T, K> grid(
            solim.signal.Readable<Integer> columnCount,
            solim.signal.Readable<? extends Iterable<T>> items,
            java.util.function.Function<T, K> keyExtractor,
            java.util.function.Function<T, solim.core.Component> itemFactory) {
        solim.layout.ReactiveGrid<T, K> grid = solim.layout.ReactiveGrid.of(columnCount, items, keyExtractor,
                itemFactory);
        ParentStack.attachToParent(grid.element());
        return grid;
    }

    public static <T> solim.core.Disposable listen(Class<T> type, arc.func.Cons<T> listener) {
        return solim.core.EventsUtil.listen(type, listener);
    }

    public static <E, T> Signal<T> createSignal(Class<E> eventType, java.util.function.Supplier<T> supplier) {
        return solim.core.EventsUtil.createSignal(eventType, supplier);
    }

    public static <E, T> Signal<T> createSignal(Class<E> eventType, arc.func.Func<E, T> mapper, T initial) {
        return solim.core.EventsUtil.createSignal(eventType, mapper, initial);
    }

    public static <T> Signal<T> createSignal(java.util.function.Consumer<Runnable> callbackRegistrar,
            java.util.function.Supplier<T> supplier) {
        return solim.core.EventsUtil.createSignal(callbackRegistrar, supplier);
    }

    public static Computed<Float> dvw(float percentage) {
        return Units.dvw(percentage);
    }

    public static Computed<Float> dvh(float percentage) {
        return Units.dvh(percentage);
    }

    public static Computed<Float> dvw(Readable<Float> percentage) {
        return Units.dvw(percentage);
    }

    public static Computed<Float> dvh(Readable<Float> percentage) {
        return Units.dvh(percentage);
    }
}

