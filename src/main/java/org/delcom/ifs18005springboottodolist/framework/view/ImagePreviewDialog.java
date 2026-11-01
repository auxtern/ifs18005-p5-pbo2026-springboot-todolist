package org.delcom.ifs18005springboottodolist.framework.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

/**
 * Modal untuk menampilkan gambar dalam ukuran diperbesar.
 */
final class ImagePreviewDialog {

    private ImagePreviewDialog() {
    }

    static void open(String imageUrl, String alt) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        Image image = new Image(imageUrl, alt == null ? "Pratinjau" : alt);
        image.getStyle()
                .set("display", "block")
                .set("max-width", "min(92vw, 960px)")
                .set("max-height", "82vh")
                .set("width", "auto")
                .set("height", "auto")
                .set("object-fit", "contain")
                .set("border-radius", "10px");

        Div body = new Div(image);
        body.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("padding", "0.35rem");

        Span hint = new Span("Klik di luar gambar untuk menutup");
        hint.getStyle()
                .set("display", "block")
                .set("margin-top", "0.65rem")
                .set("text-align", "center")
                .set("font-size", "0.8rem")
                .set("color", "#6B7280");

        Dialog dialog = new Dialog();
        dialog.add(body, hint);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setDraggable(false);
        dialog.setResizable(false);
        dialog.getElement().getStyle().set("--lumo-space-m", "0.5rem");
        dialog.open();
    }

    static Image clickableThumbnail(String imageUrl, String alt, String height) {
        return clickableThumbnail(imageUrl, alt, height, "6px");
    }

    static Image clickableThumbnail(String imageUrl, String alt, String height, String borderRadius) {
        Image image = new Image(imageUrl, alt);
        image.setHeight(height);
        image.getStyle()
                .set("border-radius", borderRadius)
                .set("object-fit", "cover")
                .set("cursor", "zoom-in")
                .set("display", "block");
        image.getElement().setAttribute("title", "Klik untuk memperbesar");
        image.addClickListener(event -> open(imageUrl, alt));
        return image;
    }
}
