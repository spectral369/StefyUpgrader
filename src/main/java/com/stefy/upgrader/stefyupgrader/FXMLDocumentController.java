package com.stefy.upgrader.stefyupgrader;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
//import com.jfoenix.controls.JFXProgressBar;
import com.stefy.upgrader.conv2.Ffmpeg;
import com.stefy.upgrader.downloader.YTDownloader;
import com.stefy.upgrader.folder.Folder;
import com.stefy.upgrader.utils.StefyCodecs;
import com.stefy.upgrader.utils.StefyFormats;
import com.stefy.upgrader.utils.StefyUtils;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.Pane;

/**
 *
 * @author spectral369
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private VBox progr;
    @FXML
    private VBox song;
    @FXML
    private Label status;
    @FXML
    private Button convert;
    @FXML
    private Button addFile;
    @FXML
    private Button addFolder;
    @FXML
    private Button clear;
    @FXML
    private Pane header;
    @FXML
    private Button cancel;
    @FXML
    private Button addLink;
    /*  @FXML 
    private Button pause;*/

    private final List<Label> labels = new ArrayList<>();
    private final List</*JFXProgressBar*/ProgressBar> pBars = new ArrayList<>();
    private final Folder f = new Folder();
    private final DirectoryChooser chooserDir = new DirectoryChooser();
    private final FileChooser chooserFile = new FileChooser();
    private String outputDir = System.getProperty("user.home") + "/converted/";
    private boolean setDelSelected = true;
    private boolean setComporessSelected = false;
    List<Future<String>> futures = new ArrayList<>();
    JFXComboBox<String> hz = new JFXComboBox<String>();
    JFXCheckBox del = new JFXCheckBox("Delete");
    TextField dest = new TextField(outputDir);
    JFXCheckBox compress = new JFXCheckBox("Reduce Size?");

    JFXComboBox<String> format = new JFXComboBox<String>();
    JFXComboBox<String> bitRateType = new JFXComboBox<String>();
    JFXComboBox<String> quality = new JFXComboBox<String>();
    public static List<String> s2 = new ArrayList<>();
    JFXCheckBox resample = new JFXCheckBox("Change bit rate");
    JFXComboBox<String> codec = new JFXComboBox<String>();

    private ExecutorService exec = null;
    private Task<Void> task = null;
    private Thread clean = null;
    private boolean isCancelled = false;
    private TextArea linksArea;

    public CountDownLatch latch = null;
    private List<String> YTLinks = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //  pause.setVisible(false);
        cancel.setVisible(false);
        convert.setDisable(true);
        addFile.setDisable(true);
        addFolder.setDisable(true);
        addLink.setDisable(true);
        clear.setDisable(true);
        del.setSelected(setDelSelected);
        dest.setEditable(false);
        bitRateType.getItems().add("CONSTANT(CBR)(best option ATM)");
        //      bitRateType.getItems().add("AVERAGE(AVR)(not available for AAC)");
        bitRateType.getItems().add("VARIABLE(VBR)(not Documented)");
        bitRateType.getSelectionModel().selectFirst();
        quality.getItems().add("Best Audio");
        quality.getItems().add("Best Video");
        quality.getSelectionModel().selectFirst();
        //  format.getItems().add("AAC");
        // format.getItems().add("M4A");
        for (StefyFormats sf : StefyFormats.values()) {
            format.getItems().add(sf.toString());
        }

        format.getSelectionModel().selectFirst();
        format.getItems().remove(format.getItems().size() - 1);

        for (StefyCodecs sc : StefyCodecs.values()) {
            codec.getItems().add(sc.toString());
        }
        codec.getSelectionModel().selectFirst();

        compress.setSelected(false);

        hz.getItems().add("default");
        hz.getItems().add("12000hz");
        hz.getItems().add("19200hz");
        hz.getItems().add("32000hz");
        hz.getItems().add("48000hz");

        hz.getSelectionModel().selectFirst();
        resample.setSelected(true);
        YTLinks = new LinkedList<>();
        //  latch =  new CountDownLatch(1);
        // exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @FXML
    protected void handleClear() {

        progr.getChildren().clear();
        song.getChildren().clear();
        pBars.clear();
        labels.clear();
        f.getAllFiles().clear();
        YTLinks.clear();
        status.setText("Cleared...");

    }

    @FXML
    protected void handleFolder() {

        //chooserDir.getExtensionFilters().removeAll(choser.getExtensionFilters());
        chooserDir.setTitle("Select a folder");
        chooserDir.setInitialDirectory(new File(System.getProperty("user.home")));

        File fol = chooserDir.showDialog(song.getScene().getWindow());
        if (fol != null && fol.isDirectory()) {

            f.setPath(fol.getPath());
            f.setAddFiles();
            setSongNames();
            in();
        }
        status.setText("Ready");

    }

    @FXML
    protected void handleFiles() {

        chooserFile.getExtensionFilters().removeAll(chooserFile.getExtensionFilters());
        chooserFile.setTitle("Select a folder");
        chooserFile.setInitialDirectory(new File(System.getProperty("user.home")));
        /*chooserFile.getExtensionFilters().addAll( //old
                new FileChooser.ExtensionFilter("MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("M4A", "*.m4a"),
                new FileChooser.ExtensionFilter("MP4", "*.mp4"),
                new FileChooser.ExtensionFilter("WEBM", "*.webm"),
                new FileChooser.ExtensionFilter("FLAC", "*.flac")
        );*/
        FileChooser.ExtensionFilter fileExtensions
                = new FileChooser.ExtensionFilter(
                        "Song Formats", "*.mp3", "*.m4a", "*.mp4", "*.webm", "*.flac");

        chooserFile.getExtensionFilters().add(fileExtensions);

        List<File> list = chooserFile.showOpenMultipleDialog(song.getScene().getWindow());
        if (list != null) {

            list.forEach((o) -> {
                if (f.getAllFiles().indexOf(o) == -1) {
                    f.addFile(o);
                }
            });
            setSongNames();
            in();

        }

        status.setText("Ready");
    }

    @FXML
    protected void handleLink() {
        final Stage dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(song.getScene().getWindow());
        dialog.resizableProperty().setValue(Boolean.FALSE);

        dialog.setResizable(false);
        dialog.setMaximized(false);
        dialog.setFullScreen(false);

        dialog.iconifiedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            dialog.close();
        });
        VBox dialogVbox = new VBox(20);

        //code
        Label addlinks = new Label("Add Youtube Links:");

        linksArea = new TextArea() {
            @Override
            public void paste() {
                final Clipboard clipboard = Clipboard.getSystemClipboard();

                if (clipboard.hasString()) {

                    String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

                    Pattern compiledPattern = Pattern.compile(pattern);
                    Matcher matcher = compiledPattern.matcher(clipboard.getString());
                    if (matcher.find()) {

                        appendText(clipboard.getString());
                        appendText(System.getProperty("line.separator"));

                    }
                }

            }

        };

        linksArea.setTooltip(new Tooltip("Paste YT links here"));
        linksArea.setPrefColumnCount(45);
        linksArea.setPrefRowCount(26);

        linksArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

            if (newValue.contains(System.getProperty("line.separator"))) {

                String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

                Pattern compiledPattern = Pattern.compile(pattern);
                Matcher matcher = compiledPattern.matcher(oldValue);
                if (!matcher.find()) {

                    Platform.runLater(() -> {
                        if (!linksArea.getText().isEmpty()) {
                            linksArea.deletePreviousChar();
                        }
                        linksArea.deleteText(linksArea.getText().indexOf(oldValue), (linksArea.getText().indexOf(oldValue) + oldValue.length()));
                    });

                }
            }

        });

        //code
        HBox h = new HBox();
        JFXButton add = new JFXButton("Add");
        add.setAlignment(Pos.BOTTOM_RIGHT);
        add.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            //add each link

            String[] token = linksArea.getText().split(System.getProperty("line.separator"));
            YTLinks.addAll(Arrays.asList(token));
            setYTLinks();
            in();
            dialog.close();

        });

        JFXButton close = new JFXButton("Close");
        close.setAlignment(Pos.BOTTOM_RIGHT);
        close.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            dialog.close();
        });
        h.getChildren().addAll(add, close);
        //  dialogVbox.getChildren().addAll(destination, dest, del, result, format, bitrate, bitRateType, qua, quality, res, resample, hz, h);

        dialogVbox.getChildren().addAll(addlinks, linksArea, h);
        Scene dialogScene = new Scene(dialogVbox, 385, 410);

        dialog.setScene(dialogScene);
        dialog.show();
        h.setPadding(new Insets(0, 0, 0, 250));

    }

    @FXML
    protected void handleOp() {
        convert.setDisable(false);
        addFile.setDisable(false);
        addFolder.setDisable(false);
        addLink.setDisable(false);
        clear.setDisable(false);

        final Stage dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(song.getScene().getWindow());
        dialog.resizableProperty().setValue(Boolean.FALSE);

        dialog.setResizable(false);
        dialog.setMaximized(false);
        dialog.setFullScreen(false);

        dialog.iconifiedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            dialog.close();
        });
        VBox dialogVbox = new VBox(20);

        //options
        Label destination = new Label("Destination: ");

        dest.setOnMouseClicked((MouseEvent e) -> {
            DirectoryChooser fc = new DirectoryChooser();
            fc.setTitle("Select a folder");
            if (new File(outputDir).exists()) {
                fc.setInitialDirectory(new File(outputDir));
            } else {
                File o = new File(outputDir);
                o.mkdir();
                fc.setInitialDirectory(o);
            }
            File d = fc.showDialog(song.getScene().getWindow());
            if (d != null) {
                dest.setText(d.getPath());
                outputDir = d.getPath() + "/";
            }

        });

        codec.setTooltip(new Tooltip("Codec that will be used!"));
        codec.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (codec.getSelectionModel().getSelectedIndex() == 1) {
                format.getItems().clear();
                for (StefyFormats sf : StefyFormats.values()) {
                    format.getItems().add(sf.toString());
                }
                format.getSelectionModel().selectLast();
                format.setDisable(true);

            } else {
                format.getItems().clear();
                for (StefyFormats sf : StefyFormats.values()) {
                    format.getItems().add(sf.toString());
                }
                format.getSelectionModel().selectFirst();
                format.getItems().remove(format.getItems().size() - 1);

                format.setDisable(false);

            }

            compress.setVisible(!compress.isVisible());
            bitRateType.getSelectionModel().selectFirst();
            //  format.setDisable(!format.isDisabled());
            hz.getSelectionModel().select(1);
            if (codec.getSelectionModel().isSelected(0)) {
                hz.getSelectionModel().selectFirst();
            }

        });

        del.setTooltip(new Tooltip("delete files after converting?"));

        del.setOnMouseClicked(e -> {
            setDelSelected = !setDelSelected;
        });

        Label result = new Label("Result");

        Label bitrate = new Label("Bit Rate Type");

        compress.setTooltip(new Tooltip("Reduce file size(Usually doesn't affect quality)"));

        compress.setOnMouseClicked(event -> {
            setComporessSelected = !setComporessSelected;
        });

        Label res = new Label("Select bit rate");//Resample
        bitRateType.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.toString().contains("VBR")) {
                if (resample != null && hz != null) {
                    res.setVisible(false);
                    if (!codec.getSelectionModel().getSelectedItem().equals(StefyCodecs.libopus.toString())) {
                        resample.setVisible(false);
                        hz.setVisible(false);
                        hz.getSelectionModel().select(1);
                    }
                }
            } else {
                if (resample != null && hz != null) {
                    hz.getSelectionModel().selectFirst();
                    resample.setVisible(true);
                    hz.setVisible(true);
                    res.setVisible(true);
                }
            }
        });

        Label qua = new Label("Download Type");

        resample.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
            if (resample.isSelected()) {
                dialogVbox.getChildren().add(dialogVbox.getChildren().size() - 1, hz);
            } else {
                dialogVbox.getChildren().remove(hz);
            }
        });

        HBox h = new HBox();

        JFXButton close = new JFXButton("Close");
        close.setAlignment(Pos.BOTTOM_RIGHT);
        close.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent mouseEvent) -> {
            dialog.close();
        });
        h.getChildren().add(close);
        h.setPadding(new Insets(0, 0, 0, 240));

        //options
        dialogVbox.getChildren().addAll(destination, dest, codec, del, result, format, compress, bitrate, bitRateType, qua, quality, res, resample, hz, h);

        Scene dialogScene = new Scene(dialogVbox, 300, 650);

        dialog.setScene(dialogScene);
        dialog.show();

    }

    public void in() {

        progr.setPadding(new Insets(5, 5, 5, 5));

        progr.setSpacing(4);//13
        song.setSpacing(8);
        song.setPadding(new Insets(5, 5, 0, 10));
        song.getChildren().clear();
        progr.getChildren().clear();

        for (int i = 0; i < labels.size(); i++) {
            progr.getChildren().add(pBars.get(i));
            song.getChildren().add(labels.get(i));

        }
        // labels.clear();
        // pBars.clear();

    }

    /*   private void addS(Label l) {

        if (labels.indexOf(l) == -1) {

            labels.add(l);
        }

    }*/

 /* private void addP(JFXProgressBar p) {
        if (pBars.indexOf(p) == -1) {
            pBars.add(p);
        }
    }
     */
    private void setSongNames() {
        labels.clear();
        pBars.clear();
        f.getAllFiles().stream().map((fi) -> new Label(fi.getName())).map((l) -> {
            l.setTooltip(new Tooltip(l.getText()));
            return l;
        }).map((l) -> {
            labels.add(l);
            return l;
        }).map((_item) -> new ProgressBar(0.0)/*new JFXProgressBar(0.0)*/).map((pro) -> {
            pro.setPrefWidth(30.0);
            return pro;
        }).map((pro) -> {
            pro.setTooltip(new Tooltip("Progress..."));
            return pro;
        }).map((pro) -> {
            pro.setId("nr");
            return pro;
        }).forEachOrdered((pro) -> {
            pBars.add(pro);
        });
    }

    private void setYTLinks() {
        labels.clear();
        pBars.clear();
        YTLinks.stream().map((fi) -> new Label(fi)).map((l) -> {
            l.setTooltip(new Tooltip(l.getText()));
            return l;
        }).map((l) -> {
            labels.add(l);
            return l;
        }).map((_item) -> new /*JFXProgressBar(0.0)*/ ProgressBar(0.0)).map((pro) -> {
            pro.setPrefWidth(30.0);
            return pro;
        }).map((pro) -> {
            pro.setTooltip(new Tooltip("Progress..."));
            return pro;
        }).map((pro) -> {
            pro.setId("nr");
            return pro;
        }).forEachOrdered((pro) -> {
            pBars.add(pro);
        });
    }

    private final AtomicBoolean isDone = new AtomicBoolean(false);

    @FXML
    protected void handleConv() {
        if (f == null) {
            return;
        } else if (f.getAllFiles().isEmpty() && YTLinks.isEmpty()) {
            return;
        }

        int sw = 0;
        if (!f.getAllFiles().isEmpty()) {
            sw = 1;
        } else if (!YTLinks.isEmpty()) {
            sw = 2;
        }

        switch (sw) {
            case 1:
                runFilesConv();
                break;
            case 2:
                runLinksConv();
                break;
            ///case 0
            ///case default
        }

    }

    private void runFilesConv() {
        isCancelled = false;
        if (!StefyUtils.isNetAvailable1()) {
            status.setText("Please check your internet connection !");
            status.setStyle("-fx-accent: red;");
        } else {
            header.setDisable(true);
            cancel.setVisible(true);

            exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            latch = new CountDownLatch(1);
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {

                    for (File a : f.getAllFiles()) {
                        futures.add(exec.submit(() -> {
                            try {
                                Platform.runLater(() -> {
                                    status.setText("Working....");
                                    int l = f.getAllFiles().indexOf(a);
                                    Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                    if (n instanceof ProgressBar/*JFXProgressBar*/) {

                                        (/*(JFXProgressBar)*/(ProgressBar) n).setProgress(0.3);
                                    }
                                });

                                final YTDownloader downloader = new YTDownloader(a, outputDir, del.isSelected(), quality.getSelectionModel().getSelectedItem().toString());

                                Platform.runLater(() -> {
                                    int l = f.getAllFiles().indexOf(a);
                                    Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                    if (n instanceof ProgressBar/*JFXProgressBar*/) {
                                        if (isCancelled) {
                                            status.setStyle("-fx-accent: red;");
                                        } else {
                                            status.setStyle("-fx-accent: green;");
                                        }
                                        (/*(JFXProgressBar)*/(ProgressBar) n).setProgress(0.4);

                                    }

                                });
                                if ((downloader.isValid) && (task.isDone())) {

                                    /*final Ffmpeg ff = */
                                    new Ffmpeg(new File(downloader.getDownloadedFileName()),
                                            format.getSelectionModel().getSelectedItem().toString(),
                                            outputDir,
                                            hertz(hz.getValue().toString()),
                                            bitRateType.getSelectionModel().getSelectedItem().toString(),
                                            setComporessSelected,
                                            codec.getSelectionModel().getSelectedItem().toString());
                                    Platform.runLater(() -> {
                                        int l = f.getAllFiles().indexOf(a);
                                        Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                        if (n instanceof ProgressBar/*JFXProgressBar*/) {
                                            if (isCancelled) {
                                                status.setStyle("-fx-accent: red;");
                                            } else {
                                                status.setStyle("-fx-accent: green;");
                                            }
                                            (/*(JFXProgressBar)*/(ProgressBar) n).setProgress(1.0);
                                        }

                                    });
                                }
                            } catch (Exception e) {
                                // e.printStackTrace();
                                Platform.runLater(() -> {
                                    int l = f.getAllFiles().indexOf(a);
                                    Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                    if (n instanceof ProgressBar/*JFXProgressBar*/) {
                                        status.setStyle("-fx-accent: red;");
                                        (/*(JFXProgressBar)*/(ProgressBar) n).setProgress(1.0);
                                    }
                                });
                            }
                            if (a.equals(f.getAllFiles().get(f.getAllFiles().size() - 1))) {

                                isDone.set(true);
                                latch.countDown();

                            }

                            return "success";
                        }));

                    }
                    /* Platform.runLater(() -> {
                        status.setText("Almost Done...");
                        exec.shutdown();
                    });*/
                    //   
                    return null;
                }

            };

            task.run();
            clean = new Thread(() -> {
                try {

                    latch.await();
                    if (isDone.get()) {

                        //futures.get(f.getAllFiles().size());
                        task.cancel(true);
                        exec.shutdown();
                        header.setDisable(false);
                        cancel.setVisible(false);
                    }
                    //task.cancel();

                    Platform.runLater(() -> {
                        if (isCancelled) {
                            status.setStyle("-fx-accent: red;");
                            status.setText("Cancelled");
                        } else {
                            status.setText("Done !");
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            clean.start();
        }
    }

    private void runLinksConv() {
        isCancelled = false;
        if (!StefyUtils.isNetAvailable1()) {
            status.setText("Please check your internet connection !");
            status.setStyle("-fx-accent: red;");
        } else {
            header.setDisable(true);
            cancel.setVisible(true);

            exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            latch = new CountDownLatch(1);
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {

                    for (String a : YTLinks) {
                        futures.add(exec.submit(() -> {
                            try {
                                Platform.runLater(() -> {
                                    status.setText("Working....");
                                    int l = YTLinks.indexOf(a);
                                    Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                    if (n instanceof ProgressBar/*JFXProgressBar*/) {

                                        (/*(JFXProgressBar) */(ProgressBar) n).setProgress(0.3);
                                    }
                                });

                                final YTDownloader downloader = new YTDownloader(a, outputDir, del.isSelected(), quality.getSelectionModel().getSelectedItem().toString());

                                Platform.runLater(() -> {
                                    int l = YTLinks.indexOf(a);
                                    Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                    if (n instanceof ProgressBar/*JFXProgressBar*/) {
                                        if (isCancelled) {
                                            status.setStyle("-fx-accent: red;");
                                        } else {
                                            status.setStyle("-fx-accent: green;");
                                        }
                                        (/*(JFXProgressBar)*/(ProgressBar) n).setProgress(0.4);

                                    }

                                });
                                if ((downloader.isValid) && (task.isDone())) {

                                    /* final Ffmpeg ff = */
                                    new Ffmpeg(new File(downloader.getDownloadedFileName()),
                                            format.getSelectionModel().getSelectedItem().toString(),
                                            outputDir,
                                            hertz(hz.getValue().toString()),
                                            bitRateType.getSelectionModel().getSelectedItem().toString(),
                                            setComporessSelected,
                                            codec.getSelectionModel().getSelectedItem().toString());
                                    Platform.runLater(() -> {
                                        int l = YTLinks.indexOf(a);
                                        Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                        if (n instanceof ProgressBar/*JFXProgressBar*/) {
                                            if (isCancelled) {
                                                status.setStyle("-fx-accent: red;");
                                            } else {
                                                status.setStyle("-fx-accent: green;");
                                            }
                                            (/*(JFXProgressBar)*/(ProgressBar) n).setProgress(1.0);
                                        }

                                    });
                                }
                            } catch (Exception e) {
                                // e.printStackTrace();
                                Platform.runLater(() -> {
                                    int l = YTLinks.indexOf(a);
                                    Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                    if (n instanceof ProgressBar/*JFXProgressBar*/) {
                                        status.setStyle("-fx-accent: red;");
                                        (/*(JFXProgressBar)*/(ProgressBar) n).setProgress(1.0);
                                    }
                                });
                            }
                            //  if (a.equals(f.getAllFiles().get(f.getAllFiles().size() - 1))) {
                            if (a.equals(YTLinks.get(YTLinks.size() - 1))) {
                                isDone.set(true);
                                latch.countDown();

                            }

                            return "success";
                        }));

                    }
                    /* Platform.runLater(() -> {
                        status.setText("Almost Done...");
                        exec.shutdown();
                    });*/
                    //   
                    return null;
                }

            };

            task.run();
            clean = new Thread(() -> {
                try {

                    latch.await();
                    if (isDone.get()) {

                        //futures.get(f.getAllFiles().size());
                        task.cancel(true);
                        exec.shutdown();
                        header.setDisable(false);
                        cancel.setVisible(false);
                    }
                    //task.cancel();

                    Platform.runLater(() -> {
                        if (isCancelled) {
                            status.setStyle("-fx-accent: red;");
                            status.setText("Cancelled");
                        } else {
                            status.setText("Done !");
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            clean.start();
        }
    }

    @FXML
    protected void handleCancel() {

        isCancelled = true;
        task.cancel(true);

        exec.shutdownNow();

        if (clean.isAlive()) {
            latch.countDown();
        }
        if (s2.size() > 0) {
            for (String s : s2) {
                System.out.println("delete " + s);
                new File(s).delete();
            }
        }
        status.setText("Cancelled");
        header.setDisable(false);
        cancel.setVisible(false);

    }

    private int hertz(String hz) {
        if (hz.equals("default")) {
            return 0;
        } else {

            switch (Integer.parseInt(hz.substring(0, hz.length() - 2))) {

                case 12000:
                    return 120;
                case 192000:
                    return 192;
                case 32000:
                    return 320;
                case 48000:
                    return 480;
            }
            return 192;
        }
    }

}
