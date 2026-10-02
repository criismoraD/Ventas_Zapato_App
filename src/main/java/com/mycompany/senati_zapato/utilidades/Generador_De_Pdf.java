package com.mycompany.senati_zapato.utilidades;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.awt.Desktop;
import java.io.File;
import java.util.List;

public class Generador_De_Pdf {

    public static void generarComprobante(Venta venta) {
        try {
            String appDir = System.getProperty("user.dir") + File.separator + "Datos_SenatiZapato";
            File dir = new File(appDir, "comprobantes");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String dest = new File(dir, "Comprobante_Venta_" + venta.Get_Id() + ".pdf").getAbsolutePath();
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            DeviceRgb primaryColor = new DeviceRgb(138, 82, 57); // Color distintivo del sistema
            DeviceRgb lightBg = new DeviceRgb(250, 246, 240);

            // --- HEADER ---
            Table headerTable = new Table(new float[]{1});
            headerTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            Cell headerCell = new Cell().add(new Paragraph("SELLO MASCULINO")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(primaryColor)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(20);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            // --- INFO DE LA VENTA (2 Columnas) ---
            Table infoTable = new Table(new float[]{1, 1});
            infoTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            
            Cell leftInfo = new Cell().setBorder(Border.NO_BORDER);
            leftInfo.add(new Paragraph("COMPROBANTE DE VENTA").setBold().setFontSize(14).setFontColor(primaryColor));
            leftInfo.add(new Paragraph("N° de Ticket: #" + String.format("%06d", venta.Get_Id())));
            leftInfo.add(new Paragraph("Fecha y Hora: " + venta.getFechaHora()));
            
            Cell rightInfo = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            rightInfo.add(new Paragraph("DATOS DEL PAGO").setBold().setFontSize(14).setFontColor(primaryColor));
            rightInfo.add(new Paragraph("Cajero: " + venta.Get_Cajero()));
            rightInfo.add(new Paragraph("Método: " + (venta.Get_Metodo_Pago() != null ? venta.Get_Metodo_Pago() : "Efectivo")));
            rightInfo.add(new Paragraph("Estado: " + venta.Get_Estado()));

            infoTable.addCell(leftInfo);
            infoTable.addCell(rightInfo);
            document.add(infoTable);

            document.add(new Paragraph("\n"));

            // --- TABLA DE DETALLES ---
            Table table = new Table(new float[]{4, 2, 2, 2});
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

            // Encabezados
            String[] headers = {"Descripción del Producto", "Precio Unitario", "Cantidad", "Subtotal"};
            for (String h : headers) {
                Cell c = new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(primaryColor)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(8)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(c);
            }

            Dao_De_Venta dao = new Dao_De_Venta();
            List<Object[]> detalles = dao.obtenerDetallesPorVenta(venta.Get_Id());

            boolean alternate = false;
            for (Object[] d : detalles) {
                com.itextpdf.kernel.colors.Color rowColor = alternate ? lightBg : ColorConstants.WHITE;
                
                table.addCell(createDataCell(String.valueOf(d[0]), rowColor, TextAlignment.LEFT));
                table.addCell(createDataCell(String.format("S/ %.2f", (Double)d[1]), rowColor, TextAlignment.CENTER));
                table.addCell(createDataCell(String.valueOf(d[2]), rowColor, TextAlignment.CENTER));
                table.addCell(createDataCell(String.format("S/ %.2f", (Double)d[3]), rowColor, TextAlignment.CENTER));
                alternate = !alternate;
            }
            document.add(table);

            // --- TOTAL ---
            Table finalTotalLayout = new Table(new float[]{7, 4});
            finalTotalLayout.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            finalTotalLayout.setMarginTop(15);
            
            finalTotalLayout.addCell(new Cell().setBorder(Border.NO_BORDER));
            
            Table boxTotal = new Table(new float[]{1, 1});
            boxTotal.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            
            double subtotal = venta.Get_Monto_Total() / 1.18;
            double igv = venta.Get_Monto_Total() - subtotal;
            
            boxTotal.addCell(new Cell().add(new Paragraph("Subtotal:")).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            boxTotal.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", subtotal))).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            
            boxTotal.addCell(new Cell().add(new Paragraph("IGV (18%):")).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            boxTotal.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", igv))).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            
            boxTotal.addCell(new Cell().add(new Paragraph("TOTAL GENERAL:").setBold().setFontSize(12))
                    .setBorder(Border.NO_BORDER)
                    .setBorderTop(new SolidBorder(primaryColor, 1))
                    .setPadding(4)
                    .setPaddingTop(8)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
                    
            boxTotal.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", venta.Get_Monto_Total())).setBold().setFontSize(16).setFontColor(primaryColor))
                    .setBorder(Border.NO_BORDER)
                    .setBorderTop(new SolidBorder(primaryColor, 1))
                    .setPadding(4)
                    .setPaddingTop(8)
                    .setTextAlignment(TextAlignment.RIGHT));

            if ("Efectivo".equalsIgnoreCase(venta.Get_Metodo_Pago())) {
                boxTotal.addCell(new Cell().add(new Paragraph("Monto Recibido:")).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
                boxTotal.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", venta.getMontoRecibido()))).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
                boxTotal.addCell(new Cell().add(new Paragraph("Vuelto:")).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
                boxTotal.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", venta.getVuelto()))).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
            } else if (venta.getReferencia() != null && !venta.getReferencia().isEmpty()) {
                boxTotal.addCell(new Cell().add(new Paragraph("Ref / Operación:")).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
                boxTotal.addCell(new Cell().add(new Paragraph(venta.getReferencia())).setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
            }
            
            finalTotalLayout.addCell(new Cell().add(boxTotal).setBorder(new SolidBorder(primaryColor, 2)).setBackgroundColor(lightBg).setPadding(5));
            
            document.add(finalTotalLayout);

            // --- FOOTER ---
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("¡Gracias por su compra!\nPara consultas o devoluciones conserve este documento en buen estado.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(new DeviceRgb(100, 100, 100));
            document.add(footer);

            document.close();

            // Abrir automáticamente
            File pdfFile = new File(dest);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Cell createDataCell(String text, com.itextpdf.kernel.colors.Color bgColor, TextAlignment alignment) {
        return new Cell().add(new Paragraph(text))
                .setBackgroundColor(bgColor)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(230, 230, 230), 1))
                .setPadding(10)
                .setTextAlignment(alignment);
    }
}
