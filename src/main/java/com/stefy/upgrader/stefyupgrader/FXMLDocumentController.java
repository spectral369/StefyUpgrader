package com.stefy.upgrader.stefyupgrader;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXProgressBar;
import com.stefy.upgrader.conv2.Ffmpeg;
import com.stefy.upgrader.downloader.YTDownloader;
import com.stefy.upgrader.folder.Folder;
import com.stefy.upgrader.utils.StefyUtils;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
    /*  @FXML 
    private Button pause;*/

    private final List<Label> labels = new ArrayList<>();
    private final List<JFXProgressBar> pBars = new ArrayList<>();
    private final Folder f = new Folder();
    private final DirectoryChooser chooserDir = new DirectoryChooser();
    private final FileChooser chooserFile = new FileChooser();
    private String outputDir = System.getProperty("user.home") + "/converted/";
    List<Future<String>> futures = new ArrayList<>();
    JFXComboBox hz = null;
    JFXCheckBox del = null;
    JFXComboBox format = null;
    JFXComboBox bitRateType = null;
    JFXComboBox quality = null;
    public static List<String> s2 = new ArrayList<>();
    JFXCheckBox resample = null;
    private ExecutorService exec = null;
    private Task task = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //  pause.setVisible(false);
        cancel.setVisible(false);
        convert.setDisable(true);
        addFile.setDisable(true);
        addFolder.setDisable(true);
        clear.setDisable(true);

    }

    @FXML
    protected void handleClear() {

        progr.getChildren().clear();
        song.getChildren().clear();
        pBars.clear();
        labels.clear();
        f.getAllFiles().clear();
        status.setText("Cleared...");

    }

    @FXML
    protected void handleFolder() {

        //chooserDir.getExtensionFilters().removeAll(choser.getExtensionFilters());
        chooserDir.setTitle("Select a folder");
        chooserDir.setInitialDirectory(new File(System.getProperty("user.home")));
        /*  chooserDir.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("*", "*")
         
         );*/
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
        chooserFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("M4A", "*.m4a")
        );

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

    boolean setDelSelected = true;

    @FXML
    protected void handleOp() {
        convert.setDisable(false);
        addFile.setDisable(false);
        addFolder.setDisable(false);
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
        TextField dest = new TextField(outputDir);
        dest.setEditable(false);

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
        del = new JFXCheckBox("Delete");
        del.setTooltip(new Tooltip("delete files after converting?"));
        del.setSelected(setDelSelected);

        del.setOnMouseClicked(e -> {
            setDelSelected = !setDelSelected;
        });

        Label result = new Label("Result");
        format = new JFXComboBox();
        format.getItems().add("AAC");
        format.getSelectionModel().selectFirst();
        Label bitrate = new Label("Bit Rate");
        bitRateType = new JFXComboBox();
        bitRateType.getItems().add("CONSTANT(CBR)(best option ATM)");
        //      bitRateType.getItems().add("AVERAGE(AVR)(not available for AAC)");
        bitRateType.getItems().add("VARIABLE(VBR)(not Documented)");

        Label res = new Label("Resample?");
        bitRateType.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if (newValue.toString().contains("VBR")) {
                if (resample != null && hz != null) {
                    res.setVisible(false);
                    resample.setVisible(false);
                    hz.setVisible(false);
                }
            } else {
                if (resample != null && hz != null) {

                    resample.setVisible(true);
                    hz.setVisible(true);
                }
            }
        });
        bitRateType.getSelectionModel().selectFirst();

        Label qua = new Label("Download Quality");
        quality = new JFXComboBox();
        quality.getItems().add("Standard");
        quality.getItems().add("High");
        quality.getSelectionModel().selectFirst();

        hz = new JFXComboBox();

        hz.getItems().add("12000hz");
        hz.getItems().add("19200hz");
        hz.getItems().add("32000hz");
        hz.getItems().add("48000hz");

        hz.getSelectionModel().select(2);

        resample = new JFXCheckBox("Resample");
        resample.setSelected(true);

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
        dialogVbox.getChildren().addAll(destination, dest, del, result, format, bitrate, bitRateType, qua, quality, res, resample, hz, h);

        Scene dialogScene = new Scene(dialogVbox, 300, 520);

        dialog.setScene(dialogScene);
        dialog.show();

    }

    public void in() {

        progr.setPadding(new Insets(7, 5, 5, 5));

        progr.setSpacing(13);
        song.setPadding(new Insets(0, 5, 0, 10));
        song.getChildren().clear();
        progr.getChildren().clear();

        for (int i = 0; i < labels.size(); i++) {
            progr.getChildren().add(pBars.get(i));
            song.getChildren().add(labels.get(i));

        }
        // labels.clear();
        // pBars.clear();

    }

    private void addS(Label l) {

        if (labels.indexOf(l) == -1) {

            labels.add(l);
        }

    }

    private void addP(JFXProgressBar p) {
        if (pBars.indexOf(p) == -1) {
            pBars.add(p);
        }
    }

    private void setSongNames() {
        labels.clear();
        pBars.clear();
        f.getAllFiles().stream().map((fi) -> new Label(fi.getName())).map((l) -> {
            l.setTooltip(new Tooltip(l.getText()));
            return l;
        }).map((l) -> {
            labels.add(l);
            return l;
        }).map((_item) -> new JFXProgressBar(0.0)).map((pro) -> {
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
        } else if (f.getAllFiles().isEmpty()) {
            return;
        }
        
        if(!StefyUtils.isNetAvailable1()){
          status.setText("Please check your internet connection !");
          status.setStyle("-fx-accent: red;");
        }
        else{
        header.setDisable(true);
        cancel.setVisible(true);

        exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
       
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (File a : f.getAllFiles()) {
                    futures.add(exec.submit(() -> {

                        Platform.runLater(() -> {
                            status.setText("Working....");
                            int l = f.getAllFiles().indexOf(a);
                            Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                            if (n instanceof JFXProgressBar) {

                                ((JFXProgressBar) n).setProgress(0.3);
                            }
                        });
                        final YTDownloader downloader = new YTDownloader(a, outputDir, del.isSelected(), quality.getSelectionModel().getSelectedItem().toString());
                        Platform.runLater(() -> {
                            int l = f.getAllFiles().indexOf(a);
                            Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                            if (n instanceof JFXProgressBar) {

                                ((JFXProgressBar) n).setProgress(0.4);
                            }

                        });
                        final Ffmpeg ff = new Ffmpeg(new File(downloader.getDownloadedFileName()), format.getSelectionModel().getSelectedItem().toString(), outputDir, hertz(hz.getValue().toString()), bitRateType.getSelectionModel().getSelectedItem().toString());
                        Platform.runLater(() -> {
                            int l = f.getAllFiles().indexOf(a);
                            Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                            if (n instanceof JFXProgressBar) {

                                ((JFXProgressBar) n).setProgress(1.0);
                            }

                        });

                        if (a.equals(f.getAllFiles().get(f.getAllFiles().size() - 1))) {
                            isDone.set(true);
                        }
                        return "success";
                    }));

                }
                Platform.runLater(() -> {
                    status.setText("Almost Done...");
                });
                //   
                return null;
            }

        };

        task.run();
        new Thread(() -> {
            while (!isDone.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (isDone.get()) {
                exec.shutdown();
                for (String j : s2) {
                    new File(j).delete();
                }

                header.setDisable(false);
                cancel.setVisible(false);
            }
            task.cancel();
            Platform.runLater(() -> {
                status.setText("Done !");
            });
        }).start();
        }

    }

    @FXML
    protected void handleCancel() {

        exec.shutdownNow();

        task.cancel(true);
        
        status.setText("Cancelled");
        header.setDisable(false);
        cancel.setVisible(false);

    }

    private int hertz(String hz) {

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
    /*
    private synchronized void wai() {
        int last = 700;
        for (int i = 0; i < 5; i++) {
            int Random = (int) (Math.random() * (600 + 1)) + 200;

            if (Random > last + 100 || Random < last - 100) {

                last = Random;
            }
        }
        try {
            Thread.currentThread().sleep(last);
        } catch (InterruptedException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/

}
