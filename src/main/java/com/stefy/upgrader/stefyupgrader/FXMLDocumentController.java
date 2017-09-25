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
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
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
    private Thread th = null;
    private Task task = null;
    List<Future<String>> futures = null;
    ExecutorService exec = null;
    JFXComboBox hz = null;
    JFXCheckBox del = null;
    JFXComboBox format = null;
    JFXComboBox bitRateType = null;
    private volatile boolean running = true;
    JFXComboBox quality  = null;
    private String s = null;
    List<String> s2 = new ArrayList<>();

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
                new FileChooser.ExtensionFilter("M4A", "*.m4a"),
                new FileChooser.ExtensionFilter("MP3", "*.mp3")
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

        dest.setOnMouseClicked(e -> {
            DirectoryChooser fc = new DirectoryChooser();
            fc.setTitle("Select a folder");
            fc.setInitialDirectory(new File(outputDir));
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

        bitRateType.getSelectionModel().selectFirst();

         Label qua =  new Label("Download Quality");
           quality =  new JFXComboBox();
             quality.getItems().add("Standard");
            quality.getItems().add("High");
            quality.getSelectionModel().selectFirst();
        Label res = new Label("Resample?");

        hz = new JFXComboBox();

        hz.getItems().add("12000hz");
        hz.getItems().add("19200hz");
        hz.getItems().add("32000hz");
        hz.getItems().add("48000hz");

        hz.getSelectionModel().select(2);

        JFXCheckBox resample = new JFXCheckBox("Resample");
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
        dialogVbox.getChildren().addAll(destination, dest, del, result, format, bitrate, bitRateType, qua,quality, res,resample, hz, h);

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
    
    private synchronized void  upd(String s){
        this.s = s;
    }

    @FXML
    protected void handleConv() {
        if (f == null) {
            return;
        } else if (f.getAllFiles().isEmpty()) {
            return;
        }

        header.setDisable(true);
        cancel.setVisible(true);
        // pause.setVisible(true);
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                running = true;
                futures = new ArrayList<>();
                exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
               
                for (File a : f.getAllFiles()) {
                    if(!running){
                        exec.shutdown();
                        break;
                    }

                    futures.add(exec.submit(() -> {

                        boolean done = false;
                        boolean isvalid = true;
                        //String s = null;
                        int last = 200;
                        for (int i = 0; i < 5; i++) {
                            int Random = (int) (Math.random() * (600 + 1)) + 200;

                            if (Random > last + 100 || Random < last - 100) {

                                last = Random;
                            }
                        }
                        Thread.currentThread().sleep(last);
                        Platform.runLater(() -> {
                            status.setText("Checking YT...");
                            int l = f.getAllFiles().indexOf(a);

                            Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));

                            if (n instanceof JFXProgressBar) {

                                ((JFXProgressBar) n).setStyle("-fx-accent: green;");
                                ((JFXProgressBar) n).setStyle("-fx-box-border: goldenrod;");
                                ((JFXProgressBar) n).setProgress(0.1);

                            }
                        });
                        //
                      

                    YTDownloader downloader = new YTDownloader(a, outputDir, del.isSelected(),quality.getSelectionModel().getSelectedItem().toString());

                        downloader.start();
                        Platform.runLater(() -> {
                            status.setText("YT download start...");
                        });
                        try {
                            downloader.join();
                            
                            isvalid = YTDownloader.isValid;
                            Platform.runLater(() -> {
                                status.setText("YT download Finished");
                                int l = f.getAllFiles().indexOf(a);
                                Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                if (n instanceof JFXProgressBar) {

                                    ((JFXProgressBar) n).setProgress(0.3);
                                }
                            });
                            // update.setmessage("finished...");
                        } catch (InterruptedException e) {

                        }

                        if (!downloader.isAlive() && isvalid) {
                            Platform.runLater(() -> {
                                status.setText("Converting...");
                                int l = f.getAllFiles().indexOf(a);
                                Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                if (n instanceof JFXProgressBar) {

                                    ((JFXProgressBar) n).setProgress(0.4);
                                }
                            });
                              //  System.out.println(downloader.getDownloadedFileName());
                              //  System.out.println(downloader.getOutputDir());
                            if (downloader.getDownloadedFileName().contains(downloader.getOutputDir())) {
                                //System.out.println("when not Download: " + downloader.getDownloadedFileName().contains(downloader.getOutputDir()));
                              //  s = downloader.getOutputDir() +downloader.getDownloadedFileName();
                              upd(downloader.getOutputDir() +downloader.getDownloadedFileName());
                               s2.add(s);
                             
                            } else {
                            //    System.out.println("when Download: " + downloader.getOutputDir() + downloader.getDownloadedFileName());
                              //  s = /*downloader.getOutputDir() + */downloader.getDownloadedFileName();
                                upd(downloader.getDownloadedFileName());
                         s2.add(s);
                            }
                                    
                            String brt = bitRateType.getSelectionModel().getSelectedItem().toString();
                            System.out.println("for song :"+a.getName()+" s= "+s);
                            Ffmpeg ff = new Ffmpeg(new File(s), format.getSelectionModel().getSelectedItem().toString(), outputDir, hertz(hz.getValue().toString()), bitRateType.getSelectionModel().getSelectedItem().toString());
                             System.out.println("etapa2");
                            boolean isBuilding = ff.build();
                            System.out.println("etapa3");
                            if (!isBuilding) {
                                Platform.runLater(() -> {
                                    int l = f.getAllFiles().indexOf(a);
                                    Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                    if (n instanceof JFXProgressBar) {
                                        ((JFXProgressBar) n).setStyle("-fx-accent: red;");

                                        ((JFXProgressBar) n).setProgress(1.0);
                                        
                                    }
                                });
                                this.cancel();
                            }
                            ff.execute();
                            System.out.println("etapa4");
                            done = true;

                        } else if (/*!downloader.isAlive() &&*/ !isvalid) {
                            Platform.runLater(() -> {
                                status.setText("Converting...");
                                int l = f.getAllFiles().indexOf(a);
                                Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                if (n instanceof JFXProgressBar) {

                                    ((JFXProgressBar) n).setProgress(0.4);
                                }
                            });
                            //AAC
                         
                            Ffmpeg ff = new Ffmpeg(a, format.getSelectionModel().getSelectedItem().toString(), outputDir, hertz(hz.getValue().toString()), bitRateType.getSelectionModel().getSelectedItem().toString());
                            ff.build();
                            ff.execute();
                            done = true;
                        }
                        if (done) {
                            Platform.runLater(() -> {
                                status.setText("File Conversion Done");
                                int l = f.getAllFiles().indexOf(a);
                                Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));

                                if (n instanceof JFXProgressBar) {

                                    ((JFXProgressBar) n).setProgress(1.0);

                                }
                            });
                            /*  if (del.isSelected() && isvalid) {
                                new File(s2.get(0)).delete();
                                System.out.println(s2.get(0));
                                s2.remove(0);
                            }*/
                        } else {
                            Platform.runLater(() -> {
                                if (!running) {
                                    status.setText("Cancelled");
                                } else {
                                    status.setText("Process failed...");
                                }
                                int l = f.getAllFiles().indexOf(a);
                                Node n = progr.getChildren().get(progr.getChildren().indexOf(pBars.get(l)));
                                if (n instanceof JFXProgressBar) {
                                    ((JFXProgressBar) n).setStyle("-fx-accent: red;");

                                    ((JFXProgressBar) n).setProgress(1.0);

                                }
                            });
                        }

                        return done ? "Success" : "Error";
                    }));

                }
                futures.forEach((fut) -> {
                    try {

                        System.out.println(fut.get());

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                exec.shutdown();
                for (String g : s2) {
                    File h = new File(g);
                    if (h.exists()) {
                        h.delete();
                    }
                }

                header.setDisable(false);
                cancel.setVisible(false);
                //   pause.setVisible(false);

                return null;

            }
        };
        th = new Thread(task);
        th.start();

    }

    @FXML
    protected void handleCancel() {
        running = false;

        exec.shutdownNow();
        task.cancel(true);
       // th.interrupt();
        status.setText("Cancel in progrress(please wait for current Thread to finish)");
        header.setDisable(false);
        cancel.setVisible(false);
        //  pause.setVisible(false);
    }

    /* @FXML
    protected void handlePause() throws InterruptedException{
        TOBE IMPL
    }*/
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

}
