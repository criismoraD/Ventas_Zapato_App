package com.mycompany.senati_zapato.servicios;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import com.google.genai.Client;
import com.google.genai.AsyncSession;
import com.google.genai.types.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.sound.sampled.*;
import javax.swing.SwingUtilities;

public class Servicio_De_Voz_En_Vivo {
    private static String Modelo_Voz_Efectivo() {
        try {
            return com.mycompany.senati_zapato.utilidades.Configuracion.Obtener_Modelo_Voz();
        } catch (Exception e) {
            return com.mycompany.senati_zapato.utilidades.Configuracion.MODELO_VOZ_PRINCIPAL;
        }
    }
    private static final AudioFormat MIC_FORMAT_PREFERIDO = new AudioFormat(16000f, 16, 1, true, false);
    private static final AudioFormat OUT_FORMAT = new AudioFormat(24000f, 16, 1, true, false);
    private static final int CHUNK_SIZE = 1024;

    private final Servicio_De_Gemini toolService;
    private final Panel_Principal frame;
    private final Consumer<String> onStatus;
    private final Consumer<String> onUserText;
    private final Consumer<String> onAssistantText;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean modelSpeaking = new AtomicBoolean(false);

    private Client client;
    private AsyncSession session;
    private TargetDataLine micLine;
    private AudioFormat micFormat;
    private SourceDataLine speakerLine;
    private Thread micThread;
    private Thread speakerThread;
    private final BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();

    public Servicio_De_Voz_En_Vivo(Panel_Principal frame, Consumer<String> onStatus,
                            Consumer<String> onUserText, Consumer<String> onAssistantText) {
        this.frame = frame;
        this.onStatus = onStatus;
        this.onUserText = onUserText;
        this.onAssistantText = onAssistantText;
        this.toolService = new Servicio_De_Gemini();
        this.toolService.setOnSetDarkMode(activar -> {
            if (frame != null) {
                frame.Establecer_Modo_Oscuro(activar);
            }
        });
    }

    public boolean Esta_En_Ejecucion() {
        return running.get();
    }

    public void start() {
        if (!running.compareAndSet(false, true)) return;
        new Thread(this::runLive, "gemini-live-voice").start();
    }

    public void stop() {
        running.set(false);
        closeAudio();
        if (session != null) {
            session.close();
        }
        if (client != null) {
            client.close();
        }
        status("Modo voz detenido.");
    }

    private void runLive() {
        try {
            status("Conectando modo voz...");
            client = Client.builder()
                    .apiKey(Servicio_De_Gemini.Obtener_Api_Key())
                    .httpOptions(HttpOptions.builder().apiVersion("v1beta").build())
                    .build();

            LiveConnectConfig config = LiveConnectConfig.builder()
                    .responseModalities("AUDIO")
                    .mediaResolution("MEDIA_RESOLUTION_MEDIUM")
                    .speechConfig(SpeechConfig.builder()
                            .voiceConfig(VoiceConfig.builder()
                                    .prebuiltVoiceConfig(PrebuiltVoiceConfig.builder().voiceName("Zephyr"))))
                    .contextWindowCompression(ContextWindowCompressionConfig.builder()
                            .triggerTokens(104857L)
                            .slidingWindow(SlidingWindow.builder().targetTokens(52428L).build())
                            .build())
                    .inputAudioTranscription(AudioTranscriptionConfig.builder())
                    .outputAudioTranscription(AudioTranscriptionConfig.builder())
                    .systemInstruction(Content.builder()
                            .role("system")
                            .parts(Part.fromText(Servicio_De_Gemini.Get_Prompt_Del_Sistema_En_Vivo())))
                    .tools(buildLiveTools())
                    .build();

            session = Conectar_Live_Con_Fallback(config);
            openAudio();
            session.receive(this::handleServerMessage);
            startMicLoop();
            status("Modo voz activo. Habla con el asistente.");
        } catch (Exception e) {
            running.set(false);
            closeAudio();
            status("No se pudo iniciar voz: " + e.getMessage());
        }
    }

    private AsyncSession Conectar_Live_Con_Fallback(LiveConnectConfig config) throws Exception {
        String principal = Modelo_Voz_Efectivo();
        String fallback = com.mycompany.senati_zapato.utilidades.Configuracion.MODELO_VOZ_FALLBACK;
        try {
            status("Conectando voz (" + principal + ")...");
            return client.async.live.connect(principal, config).join();
        } catch (Exception e1) {
            if (fallback != null && !fallback.equals(principal)) {
                try {
                    status("Principal no disponible, probando " + fallback + "...");
                    AsyncSession s = client.async.live.connect(fallback, config).join();
                    com.mycompany.senati_zapato.utilidades.Configuracion.Fijar_En_Memoria("GEMINI_LIVE_MODEL", fallback);
                    return s;
                } catch (Exception e2) {
                    throw new RuntimeException("Voz no disponible. Principal: " + e1.getMessage() + " | Fallback: " + e2.getMessage(), e2);
                }
            }
            throw e1;
        }
    }

    private List<Tool> buildLiveTools() {
        return toolService.Construir_Herramientas();
    }

    private void openAudio() throws LineUnavailableException {
        micLine = abrirMicrofonoCompatible();
        micLine.start();

        speakerLine = AudioSystem.getSourceDataLine(OUT_FORMAT);
        speakerLine.open(OUT_FORMAT, 24000);
        speakerLine.start();
        startSpeakerLoop();
    }

    private void startSpeakerLoop() {
        speakerThread = new Thread(() -> {
            try {
                while (running.get() && speakerLine != null) {
                    byte[] data = audioQueue.take();
                    if (speakerLine != null) {
                        speakerLine.write(data, 0, data.length);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "gemini-live-speaker");
        speakerThread.start();
    }

    private TargetDataLine abrirMicrofonoCompatible() throws LineUnavailableException {
        AudioFormat[] candidatos = {
            MIC_FORMAT_PREFERIDO,
            new AudioFormat(44100f, 16, 1, true, false),
            new AudioFormat(48000f, 16, 1, true, false),
            new AudioFormat(44100f, 16, 2, true, false),
            new AudioFormat(48000f, 16, 2, true, false)
        };
        for (AudioFormat candidato : candidatos) {
            TargetDataLine linea = null;
            try {
                linea = AudioSystem.getTargetDataLine(candidato);
                linea.open(candidato);
                micFormat = candidato;
                if (!candidato.matches(MIC_FORMAT_PREFERIDO)) {
                    status("Micrófono ajustado a " + (int) candidato.getSampleRate() + " Hz.");
                }
                return linea;
            } catch (LineUnavailableException | IllegalArgumentException ex) {
                if (linea != null) {
                    linea.close();
                }
            }
        }
        throw new LineUnavailableException("El micrófono no admite un formato PCM compatible.");
    }

    private void startMicLoop() {
        micThread = new Thread(() -> {
            byte[] buffer = new byte[CHUNK_SIZE];
            while (running.get() && micLine != null) {
                int read = micLine.read(buffer, 0, buffer.length);
                if (read > 0 && session != null) {
                    // Silenciar micrófono mientras el modelo habla para evitar eco/bucle
                    if (modelSpeaking.get()) {
                        continue;
                    }
                    byte[] audio = java.util.Arrays.copyOf(buffer, read);
                    session.sendRealtimeInput(LiveSendRealtimeInputParameters.builder()
                            .audio(Blob.builder().mimeType("audio/pcm;rate=" + (int) micFormat.getSampleRate()).data(audio))
                            .build());
                }
            }
        }, "gemini-live-mic");
        micThread.start();
    }

    private void handleServerMessage(LiveServerMessage message) {
        if (!running.get()) return;
        message.serverContent().ifPresent(content -> {
            content.inputTranscription().ifPresent(tr -> {
                if (tr.finished().orElse(false)) {
                    tr.text().ifPresent(text -> {
                        onUserText.accept(text);
                        String t = java.text.Normalizer.normalize(text.toLowerCase(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
                        if (t.contains("que es esto") || t.contains("pantalla") || t.contains("que ves") || t.contains("que hay aqui") || t.contains("donde estoy") || t.contains("estoy viendo")) {
                            if (frame != null && session != null) {
                                byte[] img = frame.Capturar_Pantalla_Actual();
                                if (img != null) {
                                    session.sendRealtimeInput(LiveSendRealtimeInputParameters.builder()
                                        .media(Blob.builder().mimeType("image/png").data(img).build())
                                        .build());
                                }
                            }
                        }
                    });
                }
            });
            content.outputTranscription().ifPresent(tr -> {
                if (tr.finished().orElse(false)) {
                    tr.text().ifPresent(text -> onAssistantText.accept(text));
                    // Modelo terminó de hablar → reactivar micrófono
                    modelSpeaking.set(false);
                }
            });
            content.interrupted().ifPresent(interrupted -> {
                if (interrupted && speakerLine != null) {
                    audioQueue.clear();
                    speakerLine.flush();
                    modelSpeaking.set(false);
                }
            });
            content.modelTurn().flatMap(Content::parts).ifPresent(parts -> {
                for (Part part : parts) {
                    part.inlineData().ifPresent(blob -> {
                        if (blob.data().isPresent() && speakerLine != null) {
                            // Modelo está hablando → silenciar micrófono
                            modelSpeaking.set(true);
                            byte[] data = blob.data().get();
                            audioQueue.offer(data);
                        }
                    });
                    // REMOVIDO: part.text().ifPresent(onAssistantText)
                    // La transcripción ya llega por outputTranscription,
                    // tenerlo aquí también causaba texto duplicado.
                }
            });

            // Detectar fin de turno del modelo
            content.turnComplete().ifPresent(complete -> {
                if (complete) {
                    modelSpeaking.set(false);
                }
            });
        });

        message.toolCall().ifPresent(toolCall -> toolCall.functionCalls().ifPresent(calls -> {
            List<FunctionResponse> responses = new ArrayList<>();
            for (FunctionCall call : calls) {
                String name = call.name().orElse("");
                Map<String, Object> args = call.args().orElse(Map.of());
                String result = executeLiveFunction(name, args);
                FunctionResponse.Builder builder = FunctionResponse.builder()
                        .name(name)
                        .response(Map.of("result", result));
                call.id().ifPresent(builder::id);
                responses.add(builder.build());
            }
            if (!responses.isEmpty() && session != null) {
                session.sendToolResponse(LiveSendToolResponseParameters.builder()
                        .functionResponses(responses)
                        .build());
            }
        }));
    }

    private String executeLiveFunction(String name, Map<String, Object> args) {
        if ("navegar_modulo".equals(name)) {
            String modulo = String.valueOf(args.getOrDefault("modulo", ""));
            String card = toCardName(modulo);
            if (card == null) return "Módulo no reconocido: " + modulo;
            if (frame != null) {
                SwingUtilities.invokeLater(() -> frame.Cambiar_Pestana(card));
            }
            return "Módulo abierto: " + ("Gestor".equals(card) ? "Inventario" : card);
        }
        if ("agregar_carrito".equals(name)) {
            String producto = String.valueOf(args.getOrDefault("producto", ""));
            int cantidad = 1;
            Object rawCantidad = args.get("cantidad");
            if (rawCantidad instanceof Number) {
                cantidad = ((Number) rawCantidad).intValue();
            } else if (rawCantidad instanceof String) {
                try { cantidad = Integer.parseInt((String) rawCantidad); } catch (NumberFormatException ignored) {}
            }
            if (frame == null) return "No se pudo acceder a la ventana principal.";
            final String[] result = new String[1];
            final int cantidadFinal = Math.max(1, cantidad);
            try {
                SwingUtilities.invokeAndWait(() -> result[0] = frame.Agregar_Producto_Al_Carrito(producto, cantidadFinal));
            } catch (Exception e) {
                return "No se pudo agregar al carrito: " + e.getMessage();
            }
            return result[0];
        }
        if ("procesar_pago".equals(name)) {
            if (frame == null) return "No se pudo acceder a la ventana principal.";
            final String[] result = new String[1];
            try {
                SwingUtilities.invokeAndWait(() -> result[0] = frame.Abrir_Panel_De_Pago());
            } catch (Exception e) {
                return "No se pudo abrir el panel de pago: " + e.getMessage();
            }
            return result[0];
        }
        if ("cancelar_orden".equals(name)) {
            if (frame == null) return "No se pudo acceder a la ventana principal.";
            final String[] result = new String[1];
            try {
                SwingUtilities.invokeAndWait(() -> result[0] = frame.Vaciar_Carrito());
            } catch (Exception e) {
                return "No se pudo vaciar el carrito: " + e.getMessage();
            }
            return result[0];
        }
        String result = toolService.Ejecutar_Funcion(name, args);
        if (onAssistantText != null && shouldShowToolResult(name)) {
            onAssistantText.accept(result);
        }
        return result;
    }

    private boolean shouldShowToolResult(String name) {
        return "ventas_hoy".equals(name)
                || "ventas_recientes".equals(name)
                || "detalle_venta".equals(name)
                || "top_productos".equals(name)
                || "ventas_ultimos_7_dias".equals(name)
                || "listar_productos".equals(name)
                || "buscar_producto".equals(name)
                || "verificar_stock".equals(name)
                || "resumen_base_datos".equals(name)
                || "consultar_base_datos".equals(name);
    }

    private String toCardName(String modulo) {
        String value = java.text.Normalizer.normalize(modulo.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        if (value.contains("inventario") || value.contains("gestor")) return "Gestor";
        if (value.contains("venta")) return "Ventas";
        if (value.contains("reporte")) return "Reportes";
        if (value.contains("inicio") || value.contains("principal")) return "Inicio";
        return null;
    }

    private void closeAudio() {
        audioQueue.clear();
        if (speakerThread != null) {
            speakerThread.interrupt();
            speakerThread = null;
        }
        try {
            if (micLine != null) {
                micLine.stop();
                micLine.close();
            }
        } catch (Exception ignored) {}
        try {
            if (speakerLine != null) {
                speakerLine.stop();
                speakerLine.close();
            }
        } catch (Exception ignored) {}
    }

    private void status(String text) {
        if (onStatus != null) onStatus.accept(text);
    }
}
