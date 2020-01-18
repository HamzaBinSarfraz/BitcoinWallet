/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.o3.bitcoin.ui.dialogs.screens;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.o3.bitcoin.hdwallet.HDAccount;
import com.o3.bitcoin.hdwallet.HDKeyChain;
import com.o3.bitcoin.model.Config;
import com.o3.bitcoin.model.manager.ConfigManager;
import com.o3.bitcoin.model.manager.WalletManager;
import com.o3.bitcoin.qrcode.QRCodeUtil;
import com.o3.bitcoin.service.WalletService;
import com.o3.bitcoin.ui.component.XButtonFactory;
import com.o3.bitcoin.ui.dialogs.DlgQRCode;
import com.o3.bitcoin.ui.dialogs.DlgRequestPayment;
import com.o3.bitcoin.ui.dialogs.RequestPaymentHandler;
import com.o3.bitcoin.ui.screens.dashboard.PnlDashboardScreen;
import com.o3.bitcoin.ui.screens.settings.PnlSettingsScreen;
import com.o3.bitcoin.ui.screens.wallet.PnlWalletScreen;
import com.o3.bitcoin.util.BitcoinCurrencyRateApi;
import com.o3.bitcoin.util.ResourcesProvider;
import com.o3.bitcoin.util.ResourcesProvider.Colors;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PnlNewRequestPaymentScreen extends javax.swing.JPanel {
    private DlgRequestPayment dlgRequestPayment;
    private boolean btcFocus = false;
    private boolean fiatFocus = false;
    public static ArrayList<RequestPaymentHandler> dataList = new ArrayList<RequestPaymentHandler>();
    public static String xmlFilePath = null;
    private String status = "pending";
    private JPopupMenu popup = null;
    private HDAccount hd = null;
    public static ArrayList<String> addressList = new ArrayList<String>();
    public static ArrayList<String> check = new ArrayList<String>();
    public String qrcodeFilePath = null;
    private static int count = 0;
    private String address;
    private String btcAmount;
    private String description;
    int index = 0;
    private String date = null;
    public static Document document1 = null;
    public static RequestPaymentHandler rawData1 = null;

    public PnlNewRequestPaymentScreen(DlgRequestPayment dlgRequestPayment) {
        dataList.clear();
        this.dlgRequestPayment = dlgRequestPayment;
        initComponents();
        createFile();
        customizeUI();
        readPaymentRequests();
        populateJTable();
        popUpReady();
        checkStatusLifeCycle();
        settingAddress();
        loadQRCode(txtAddress.getText(), "0.00", "".replaceAll("[\\s|\\u00A0]+", ""));
        startTimerToUpdateStatus();
        TimerToDeleteExpiredRequests();
        gettingAddresses();
        jComboBox1.setVisible(false);
        cmbRequestExpires.setVisible(true);
        if (BitcoinCurrencyRateApi.currentRate != null) {
            txtBTC.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent de) {
                    if (txtBTC.getText().isEmpty()) {
                        if (btcFocus) {
                            txtFiat.setText("");
                        }
                        return;
                    }
                    if (txtBTC.getText().equals(".")) {
                        if (btcFocus) {
                            txtBTC.setCaretPosition(1);
                        }
                        return;
                    }
                    if (btcFocus) {
                        Double fiatValue = (Double.parseDouble(txtBTC.getText()) * BitcoinCurrencyRateApi.currentRate.getValue());
                        txtFiat.setText(String.format("%.2f", fiatValue));
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent de) {
                    if (txtBTC.getText().isEmpty()) {
                        if (btcFocus) {
                            txtFiat.setText("");
                        }
                        return;
                    }
                    if (txtBTC.getText().equals(".")) {
                        if (btcFocus) {
                            return;
                        }
                    }
                    if (btcFocus) {
                        Double fiatValue = (Double.parseDouble(txtBTC.getText()) * BitcoinCurrencyRateApi.currentRate.getValue());
                        txtFiat.setText(String.format("%.2f", fiatValue));
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent de) {
                    if (txtBTC.getText().isEmpty()) {
                        if (btcFocus) {
                            txtFiat.setText("");
                        }
                        return;
                    }
                    if (txtBTC.getText().equals(".")) {
                        if (btcFocus) {
                            return;
                        }
                    }
                    if (btcFocus) {
                        Double fiatValue = (Double.parseDouble(txtBTC.getText()) * BitcoinCurrencyRateApi.currentRate.getValue());
                        txtFiat.setText(String.format("%.2f", fiatValue));
                    }
                }
            });
            txtFiat.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent de) {
                    if (txtFiat.getText().isEmpty()) {
                        if (fiatFocus) {
                            txtBTC.setText("");
                        }
                        return;
                    }
                    if (txtFiat.getText().equals(".")) {
                        return;
                    }

                    if (fiatFocus) {
                        Double btcValue = (Double.parseDouble(txtFiat.getText()) / BitcoinCurrencyRateApi.currentRate.getValue());
                        txtBTC.setText(String.format("%.6f", btcValue));
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent de) {
                    if (txtFiat.getText().isEmpty()) {
                        if (fiatFocus) {
                            txtBTC.setText("");
                        }
                        return;
                    }
                    if (txtFiat.getText().equals(".")) {
                        return;
                    }
                    if (fiatFocus) {
                        Double btcValue = (Double.parseDouble(txtFiat.getText()) / BitcoinCurrencyRateApi.currentRate.getValue());
                        txtBTC.setText(String.format("%.6f", btcValue));
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent de) {
                    if (txtFiat.getText().isEmpty()) {
                        if (fiatFocus) {
                            txtBTC.setText("");
                        }
                        return;
                    }
                    if (txtFiat.getText().equals(".")) {
                        return;
                    }
                    if (fiatFocus) {
                        Double btcValue = (Double.parseDouble(txtFiat.getText()) / BitcoinCurrencyRateApi.currentRate.getValue());
                        txtBTC.setText(String.format("%.6f", btcValue));
                    }
                }
            });
        }
        String selectedCurrency = ConfigManager.config().getSelectedCurrency();
        if (selectedCurrency != null && !selectedCurrency.isEmpty()) {
            lblFiat.setText("Fiat (" + selectedCurrency + ")");
        }
    }

    public void setAmount(String amount) {
        float btcAmount = (float) (Long.parseLong(amount) / 100000000.0f);
        txtBTC.setText(String.format("%.5f", btcAmount));
        try {
            Double fiatValue = (Double.parseDouble(txtBTC.getText()) * BitcoinCurrencyRateApi.currentRate.getValue());
            txtFiat.setText(String.format("%.2f", fiatValue));
        } catch (Exception e) {
            txtFiat.setText("");
        }
    }

    private void createFile() {
        Config config = ConfigManager.config();
        if("MAINNET".equals(config.getDefaultNetwork()))
        {
            try {
            File dir = new File(ConfigManager.CONFIG_ROOT + File.separator + "testrequests");
            xmlFilePath = dir + File.separator + "paymentRequestsMainNet.xml";
            if (!dir.exists()) {
                dir.mkdir();
                File file = new File(xmlFilePath);
                file.createNewFile();
                System.out.println("directory and file was created successfully");
                } else {
                    System.out.println("directory and file already exist...");
                }
            } catch (IOException e) {
                System.out.println("Exception Occurred in PnlNewRequestPaymentScreen.java");
            }
        }
        else
        {
            try {
            File dir = new File(ConfigManager.CONFIG_ROOT + File.separator + "testrequests");
            xmlFilePath = dir + File.separator + "paymentRequests.xml";
            if (!dir.exists()) {
                dir.mkdir();
                File file = new File(xmlFilePath);
                file.createNewFile();
                System.out.println("directory and file was created successfully");
                } else {
                    System.out.println("directory and file already exist...");
                }
            } catch (IOException e) {
                System.out.println("Exception Occurred in PnlNewRequestPaymentScreen.java");
            }
        }
    }

    private void writePaymentRequests(ArrayList<RequestPaymentHandler> list) {
        try {
            int counter = 0;
            Element company = new Element("TransactionRequests");
            Document document = new Document(company);
            for (RequestPaymentHandler handler : list) {
                Element employee = new Element("Requests");
                employee.setAttribute(new Attribute("id", "" + counter++));
                employee.addContent(new Element("account").setText(handler.getAccount()));
                employee.addContent(new Element("address").setText(handler.getAddress()));
                employee.addContent(new Element("date").setText(handler.getDate()));
                employee.addContent(new Element("description").setText(handler.getDescription()));
                employee.addContent(new Element("amount").setText(handler.getAmount()));
                employee.addContent(new Element("expiretime").setText(handler.getExpireTime()));
                employee.addContent(new Element("status").setText(handler.getStatus()));
                document.getRootElement().addContent(employee);
            }
            XMLOutputter xmlOutputer = new XMLOutputter();
            // you can use this tou output the XML content to
            // the standard output for debugging purposes 
            // new XMLOutputter().output(doc, System.out);
            // write the XML File with a nice formating and alignment
            xmlOutputer.setFormat(Format.getPrettyFormat());
            xmlOutputer.output(document, new FileWriter(xmlFilePath));
            System.out.println("XML File was created successfully!");
        } catch (IOException io) {
            System.out.println("Exception Handled in writePaymentRequest");
        }
    }

    private void readPaymentRequests() 
    {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = (Document) builder.build(xmlFilePath);
            Element rootNode = document.getRootElement();
            List list = rootNode.getChildren("Requests");
            dataList.clear();
            for (int i = 0; i < list.size(); i++) {
                Element node = (Element) list.get(i);
                Attribute attribute = node.getAttribute("id");
                dataList.add(new RequestPaymentHandler(node.getChildText("account"), node.getChildText("address"),
                        node.getChildText("date"), node.getChildText("description"), node.getChildText("amount"),
                        node.getChildText("expiretime"), node.getChildText("status"), attribute.getValue()));
            }
        }
        catch (IOException | JDOMException io) { }
    }

    private void afterDelete() {
        int counter = 0;
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = (Document) builder.build(xmlFilePath);
            Element rootNode = document.getRootElement();
            List list = rootNode.getChildren("Requests");
            dataList.clear();
            for (int i = 0; i < list.size(); i++) {
                Element node = (Element) list.get(i);
                dataList.add(new RequestPaymentHandler(node.getChildText("account"), node.getChildText("address"),
                        node.getChildText("date"), node.getChildText("description"), node.getChildText("amount"),
                        node.getChildText("expiretime"), node.getChildText("status"), node.setAttribute(new Attribute("id", "" + counter++)).toString()));
            }
            XMLOutputter xmlOutputer = new XMLOutputter();
            xmlOutputer.setFormat(Format.getPrettyFormat());
            xmlOutputer.output(document, new FileWriter(xmlFilePath));
        } 
        catch (IOException | JDOMException io) { }
    }
    
    private void readDashboardScreenTable()
    {
        // Calling the method to get all addresses of Payment Request Screen
        gettingAddresses();
        int tableLength = PnlDashboardScreen.tblTransactions.getRowCount();
        ArrayList<String> transactionAddresses = new ArrayList<String>();
        ArrayList<String> RequestsDates = new ArrayList<String>();
        ArrayList<String> RequestsStatus = new ArrayList<String>();
        transactionAddresses.clear();
        if(tableLength <= 0){
            System.out.println("Dashboard Screen's Table is Already Empty");
        }
        else{
            // Getting all addresses of completed Transactions 
            for (int g = 0 ; g < tableLength ; g++){
                String latestAddress = PnlDashboardScreen.tblTransactions.getValueAt(g, 2).toString();
                transactionAddresses.add(latestAddress);
            }
            
            // Getting all Dates from Payment Requests Table
            try
            {
                for (int g = 0 ; g < addressList.size() ; g++){
                    String currentDate = jTable1.getValueAt(g, 0).toString();
                    String currentStatus = jTable1.getValueAt(g, 3).toString();
                    RequestsDates.add(currentDate);
                    RequestsStatus.add(currentStatus);
                }
                
                // Checking whether status should be paid or not
                for (int i = 0 ; i < addressList.size() ; i++){
                    String testCheck = addressList.get(i);
                    if(transactionAddresses.contains(testCheck)){
                        updateStatusPreLoading("Paid", "", testCheck);
                        afterDelete();
                        readPaymentRequests();
                        populateJTable();
                    }
                }
            }
            catch(Exception ex){ }
        }
    }
    
    private void updatePaymentRequests(String address, String description, String amount) {
        List<String> result = getCurrentDate();
        if (result.get(1).equals(address)) {
            editXML(description, amount);
        }
    }

    private void editXML(String description, String amount) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = (Document) builder.build(xmlFilePath);
            Element rootNode = document.getRootElement();
            // iterating over the children
            List<Element> requests = rootNode.getChildren("Requests");
            for (Element element : requests) {
                if (date.equals(element.getChild("date").getText())) {
                    element.getChild("description").setText(description);
                    element.getChild("amount").setText(amount);
                }
            }
            XMLOutputter xmlOutput = new XMLOutputter();
            // display xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            //xmlOutput.output(document, System.out);
            xmlOutput.output(document, new FileWriter(xmlFilePath));
            readPaymentRequests();
        } catch (JDOMException ex) {
            java.util.logging.Logger.getLogger(PnlNewRequestPaymentScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PnlNewRequestPaymentScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void deleteXML(String deleteRow) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = (Document) builder.build(xmlFilePath);
            Element rootNode = document.getRootElement();
            List list = rootNode.getChildren("Requests");
            dataList.clear();
            for (int i = 0; i < list.size(); i++) {
                Element node = (Element) list.get(i);
                if (deleteRow.equals(node.getChild("date").getText())) {
                    node.removeContent();
                    node.removeAttribute("id");
                    node.setName("deleted");
                    System.out.println("Deleted...");
                }
            }
            XMLOutputter xmlOutput = new XMLOutputter();
            // display xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileWriter(xmlFilePath));
            afterDelete();
            readPaymentRequests();
        } catch (IOException | JDOMException io) { }
    }
    
    private void updateStatusPreLoading(String status, String date, String address) {
        try {   
            SAXBuilder builder = new SAXBuilder();
            Document document = (Document) builder.build(xmlFilePath);
            Element rootNode = document.getRootElement();

            if(address.equals("")){
                // iterating over the children
                List<Element> requests = rootNode.getChildren("Requests");
                for (Element element : requests) {
                    if (date.equals(element.getChild("date").getText())) {
                        element.getChild("status").setText(status);
                    }
                }
            }
            else {
                // iterating over the children
                List<Element> requests = rootNode.getChildren("Requests");
                for (Element element : requests) {
                    if (address.equals(element.getChild("address").getText())) {
                        element.getChild("status").setText(status);
                    }
                }
            }
            XMLOutputter xmlOutput = new XMLOutputter();
            // display xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileWriter(xmlFilePath));
        }
        catch (JDOMException | IOException ex) {
            java.util.logging.Logger.getLogger(PnlNewRequestPaymentScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * function that creates and loads QRCode from address
     */
    public void loadQRCode(String address, String btcAmount, String description) {
        try {
            String tmpDirPath = ConfigManager.CONFIG_ROOT + File.separator + "tmp";
            File tmpDir = new File(tmpDirPath);
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            qrcodeFilePath = tmpDirPath + File.separator + "qrcode" + count + ".png";
            if (count > 0) {
                String oldQrcodeFilePath = tmpDirPath + File.separator + "qrcode" + (count - 1) + ".png";
                File file = new File(oldQrcodeFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            String charset = "UTF-8";
            Map hintMap = new HashMap();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeUtil.createQRCode("bitcoin:" + address + "?" + "amount=" + btcAmount + "&" + "label=label&" + "message=" + description, qrcodeFilePath, charset, hintMap, 200, 200);
            lblQrcode.setIcon(new ImageIcon(qrcodeFilePath));
            count++;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        pnlProgress = new javax.swing.JPanel();
        lblAmount = new javax.swing.JLabel();
        lblAccount = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblBTC = new javax.swing.JLabel();
        lblFiat = new javax.swing.JLabel();
        txtBTC = new javax.swing.JTextField();
        txtFiat = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        lblReceiverAddress = new javax.swing.JLabel();
        lblRequestExpires = new javax.swing.JLabel();
        cmbRequestExpires = new javax.swing.JComboBox();
        btnSaveRecord = new javax.swing.JButton();
        btnNewRecord = new javax.swing.JButton();
        txtAccount = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        lblQrcode = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<String>();
        txtAddress = new javax.swing.JLabel();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        setPreferredSize(new java.awt.Dimension(900, 490));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(80, 166));
        jPanel1.setName(""); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(900, 490));
        jPanel1.setRequestFocusEnabled(false);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlProgress.setLayout(new java.awt.BorderLayout());
        jPanel1.add(pnlProgress, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        lblAmount.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        lblAmount.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        lblAmount.setText("Requested Amount");
        jPanel1.add(lblAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 160, 110, 25));

        lblAccount.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        lblAccount.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        lblAccount.setText("Account:");
        lblAccount.setMaximumSize(new java.awt.Dimension(59, 14));
        lblAccount.setMinimumSize(new java.awt.Dimension(59, 14));
        jPanel1.add(lblAccount, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, 110, 25));

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblBTC.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        lblBTC.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        lblBTC.setText("BTC");
        jPanel2.add(lblBTC, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, 30, 30));

        lblFiat.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        lblFiat.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        lblFiat.setText("Fiat");
        jPanel2.add(lblFiat, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 4, -1, 20));

        txtBTC.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        txtBTC.setPreferredSize(new java.awt.Dimension(59, 33));
        txtBTC.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtBTCFocusGained(evt);
            }
        });
        txtBTC.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBTCKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBTCKeyTyped(evt);
            }
        });
        jPanel2.add(txtBTC, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 23, 140, 25));

        txtFiat.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        txtFiat.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtFiatFocusGained(evt);
            }
        });
        txtFiat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFiatKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtFiatKeyTyped(evt);
            }
        });
        jPanel2.add(txtFiat, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 23, 150, 25));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 140, 300, -1));

        jLabel1.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        jLabel1.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        jLabel1.setText("Description:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 200, 110, 25));

        txtDescription.setColumns(20);
        txtDescription.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        txtDescription.setRows(3);
        txtDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDescriptionKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(txtDescription);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 200, 300, 50));

        lblReceiverAddress.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        lblReceiverAddress.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        lblReceiverAddress.setText("Address");
        jPanel1.add(lblReceiverAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, 110, 25));

        lblRequestExpires.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        lblRequestExpires.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        lblRequestExpires.setText("Request Expires");
        jPanel1.add(lblRequestExpires, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, 110, 25));

        cmbRequestExpires.setBackground(ResourcesProvider.Colors.SCREEN_TOP_PANEL_BG_COLOR);
        cmbRequestExpires.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        cmbRequestExpires.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        cmbRequestExpires.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 Hour", "1 Day", "1 Week", "1 Month" }));
        cmbRequestExpires.setPreferredSize(new java.awt.Dimension(275, 31));
        jPanel1.add(cmbRequestExpires, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 110, 300, 25));

        btnSaveRecord.setText("Save Request");
        btnSaveRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveRecords(evt);
            }
        });
        jPanel1.add(btnSaveRecord, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 260, 110, 25));

        btnNewRecord.setText("New Request");
        btnNewRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewRecords(evt);
            }
        });
        jPanel1.add(btnNewRecord, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 260, 110, 25));

        txtAccount.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        txtAccount.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        jPanel1.add(txtAccount, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 30, 290, 25));

        jTable1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Description", "Amount", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setGridColor(Colors.NAV_MENU_ITEM_BORDER_COLOR);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClickedListener(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable1MouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
            jTable1.getColumnModel().getColumn(3).setResizable(false);
        }

        jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(3, 310, 900, 180));

        lblQrcode.setBackground(new java.awt.Color(255, 255, 255));
        lblQrcode.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        lblQrcode.setOpaque(true);
        lblQrcode.setPreferredSize(new java.awt.Dimension(200, 200));
        lblQrcode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                generateQRCode(evt);
            }
        });
        jPanel1.add(lblQrcode, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 50, -1, -1));

        jComboBox1.setBackground(ResourcesProvider.Colors.SCREEN_TOP_PANEL_BG_COLOR);
        jComboBox1.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        jComboBox1.setForeground(ResourcesProvider.Colors.DEFAULT_HEADING_COLOR);
        jPanel1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 110, 300, 25));

        txtAddress.setFont(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
        txtAddress.setForeground(ResourcesProvider.Colors.NAV_MENU_WALLET_COLOR);
        txtAddress.setToolTipText("Click to copy address to clipboard");
        txtAddress.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        txtAddress.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtAddressMouseClicked(evt);
            }
        });
        jPanel1.add(txtAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 70, 290, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mouseClickedListener(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClickedListener
        // TODO add your handling code here:
        int row = jTable1.rowAtPoint(evt.getPoint());
        int column = jTable1.columnAtPoint(evt.getPoint());
        if (row >= 0 && column >= 0) {
            index = jTable1.getSelectedRow();
            date = jTable1.getModel().getValueAt(jTable1.getSelectedRow(), 0).toString();
            getSelectedItemsFromTable();
            if (checkStatusLifeCycle()) {
                return;
            }
        }
    }//GEN-LAST:event_mouseClickedListener
    
    private void addNewRecords(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewRecords
        WalletService currentService = WalletManager.get().getCurentWalletService();
        txtAddress.setText(currentService.getWalletFirstReceiveAddress().toString());
        clearFields();
    }//GEN-LAST:event_addNewRecords

    private void saveRecords(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveRecords
        WalletService currentService = WalletManager.get().getCurentWalletService();
        String ChangedLabelAddress = currentService.getWalletFirstReceiveAddress().toString();
        try {
            if(txtFiat.getText().equals("")){
                JOptionPane.showMessageDialog(null, "Amount can not be empty. Please enter some amount", "Empty Field Error", JOptionPane.WARNING_MESSAGE);    
            }
            else if(txtBTC.getText().equals("")){
                JOptionPane.showMessageDialog(null, "Amount can not be empty. Please enter some amount", "Empty Field Error", JOptionPane.WARNING_MESSAGE);                
            }
            else if(txtDescription.getText().equals("")){
                JOptionPane.showMessageDialog(null, "Description Field can not be Empty", "Empty Field Error", JOptionPane.WARNING_MESSAGE);
            }
            else if (!(Pattern.matches("[0-9]+", txtBTC.getText()) || Pattern.matches("[0-9.]+", txtBTC.getText()))){
                JOptionPane.showMessageDialog(null, "You Can not Enter String or Special Characters in Amount Field", "Amount Field Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            else if (!(Pattern.matches("[0-9]+", txtFiat.getText()) || Pattern.matches("[0-9.]+", txtFiat.getText()))){
                JOptionPane.showMessageDialog(null, "You Can not Enter String or Special Characters in Amount Field", "Amount Field Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            else{
                String id = "";
                final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                String getAddress = txtAddress.getText();
                String getAccount = txtAccount.getText();
                String getExpireTime = String.valueOf(cmbRequestExpires.getSelectedItem());
                String getBTCAmount = txtBTC.getText();
                String getDescription = txtDescription.getText();
            
                if (!checkDuplicateEntities(getAddress)) {
                    addPaymentRequests(getAccount, getAddress, sdf, date, getDescription, getBTCAmount, getExpireTime, id);
                }
                else {
                    updatePaymentRequests(getAddress, getDescription, getBTCAmount);
                    populateJTable();
                }
                // Getting unique address for new request
                gettingAddresses();
                try {
                    SAXBuilder builder = new SAXBuilder();
                    Document document = (Document) builder.build(xmlFilePath);
                    Element rootNode = document.getRootElement();
                    List list = rootNode.getChildren("Requests");
                    
                    for (int i = 0; i < list.size(); i++) {
                        if (addressList.contains(ChangedLabelAddress)) {
                            ChangedLabelAddress = hd.nextChangeAddress().toString();
                        } else {
                            break;
                        }
                    }
                } catch (JDOMException | IOException ex) { }   
                txtAddress.setText(ChangedLabelAddress);
    
                /**
                 * ******Setting Environment for New Request*******
                 */
                jComboBox1.setVisible(false);
                cmbRequestExpires.setVisible(true);
                cmbRequestExpires.setSelectedIndex(0);
                txtBTC.setText("");
                txtFiat.setText("");
                txtDescription.setText("");
                lblQrcode.setIcon(null);
                lblQrcode.setBackground(Color.WHITE);        
                loadQRCode(txtAddress.getText(), "0.00", "".replaceAll("[\\s|\\u00A0]+", ""));
            }
        } 
        catch (Exception e) {
            System.out.println("Null Pointer Exception");
        }
    }//GEN-LAST:event_saveRecords

    private void txtFiatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFiatFocusGained
        btcFocus = false;
        fiatFocus = true;
    }//GEN-LAST:event_txtFiatFocusGained

    private void txtBTCFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBTCFocusGained
        btcFocus = true;
        fiatFocus = false;
    }//GEN-LAST:event_txtBTCFocusGained

    private void generateQRCode(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_generateQRCode
        if (!address.equals("")) {
            DlgQRCode dlgQrcode = new DlgQRCode(address, btcAmount, description.replaceAll("[\\s|\\u00A0]+", ""));
            dlgQrcode.centerOnScreen();
            dlgQrcode.setVisible(true);
        }
    }//GEN-LAST:event_generateQRCode

    private void jTable1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseReleased
        if (SwingUtilities.isRightMouseButton(evt)) {
            if (evt.isPopupTrigger()) {
                JTable source = (JTable) evt.getSource();
                int row = source.rowAtPoint(evt.getPoint());
                int column = source.columnAtPoint(evt.getPoint());
                if (!source.isRowSelected(row)) {
                    source.changeSelection(row, column, false, false);
                }
                popup.show(evt.getComponent(), evt.getX(), evt.getY());
            }
            // HighLighting The Selected Row
            int r = jTable1.rowAtPoint(evt.getPoint());
            if (r >= 0 && r < jTable1.getRowCount()) {
                jTable1.setRowSelectionInterval(r, r);
            } else {
                jTable1.clearSelection();
                System.out.println("Cleared Selection");
            }

            int rowindex = jTable1.getSelectedRow();

            if (rowindex < 0) {
            } else {

            }
        }
    }//GEN-LAST:event_jTable1MouseReleased

    private void txtBTCKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBTCKeyTyped
        try {    
            char vChar = evt.getKeyChar();
            if (!(Character.isDigit(vChar)
                    || (vChar == KeyEvent.VK_BACK_SPACE)
                    || (vChar == KeyEvent.VK_DELETE))) {
                evt.consume();
            }
        } catch(Exception exception){
            System.out.println("txtBTCKeyTyped Handled");
        }
    }//GEN-LAST:event_txtBTCKeyTyped

    private void txtFiatKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFiatKeyTyped
        try {
            char vChar = evt.getKeyChar();
            if (!(Character.isDigit(vChar)
                    || (vChar == KeyEvent.VK_BACK_SPACE)
                    || (vChar == KeyEvent.VK_DELETE))) {
                evt.consume();
            }
        } catch (Exception e) {
            System.out.println("txtFiatKeyTyped Handled");
        }
    }//GEN-LAST:event_txtFiatKeyTyped

    private void txtAddressMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtAddressMouseClicked
        String address = txtAddress.getText();
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(new StringSelection(address), null);
    }//GEN-LAST:event_txtAddressMouseClicked

    private void txtBTCKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBTCKeyReleased
        loadQRCode(txtAddress.getText(), txtBTC.getText(), txtDescription.getText().replaceAll("[\\s|\\u00A0]+", ""));
    }//GEN-LAST:event_txtBTCKeyReleased

    private void txtFiatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFiatKeyReleased
        loadQRCode(txtAddress.getText(), txtBTC.getText(), txtDescription.getText().replaceAll("[\\s|\\u00A0]+", ""));
    }//GEN-LAST:event_txtFiatKeyReleased

    private void txtDescriptionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescriptionKeyReleased
        loadQRCode(txtAddress.getText(), txtBTC.getText(), txtDescription.getText().replaceAll("[\\s|\\u00A0]+", ""));
    }//GEN-LAST:event_txtDescriptionKeyReleased

    public void settingAddress()
    {
        String temp = PnlWalletScreen.lblAddress.getText();
        txtAddress.setText(temp);
    }
    private String getDescription(int Index) {
        RequestPaymentHandler rawData = dataList.get(Index);
        String sentDescription = rawData.getDescription();
        return sentDescription;
    }

    private String getURI(int Index) {
        RequestPaymentHandler rawData = dataList.get(Index);
        String sentURI = rawData.getAddress();
        return sentURI;
    }

    private void popUpReady() {
        popup = new JPopupMenu();
        final JMenuItem copyDescription = new JMenuItem("Copy Description");
        final JMenuItem copyURI = new JMenuItem("Copy URI");
        final JMenuItem deleteRow = new JMenuItem("Delete Row");

        // When Copy Description is Clicked
        copyDescription.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modifiedIndex = (jTable1.getRowCount() - (jTable1.getSelectedRow() + 1));
                String receiveDescription = getDescription(modifiedIndex);
                StringSelection stringSelection = new StringSelection(receiveDescription);
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });

        // When Copy URI is Clicked
        copyURI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int modifiedIndex = (jTable1.getRowCount() - (jTable1.getSelectedRow() + 1));
                String receiveURI = getURI(modifiedIndex);
                StringSelection stringSelection = new StringSelection(receiveURI);
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
            }
        });

        // When Delete Row is Clicked
        deleteRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTable1.getSelectedRow() != -1) {
                    String deleteRow = jTable1.getModel().getValueAt(jTable1.getSelectedRow(), 0).toString();
                    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                    model.removeRow(jTable1.getSelectedRow());
                    deleteXML(deleteRow);
                    settingAddress();
                    clearFields();
                }
            }
        });
        popup.add(copyDescription);
        popup.add(copyURI);
        popup.add(new JSeparator());
        popup.add(deleteRow);
    }

    private void addPaymentRequests(String getAccount, String getAddress, final DateFormat sdf, Date date, String getDescription, String getBTCAmount, String getExpireTime, String id) {
        dataList.add(new RequestPaymentHandler(getAccount, getAddress, sdf.format(date), getDescription,
                getBTCAmount, getExpireTime, status, id));
        writePaymentRequests(dataList);
        populateJTable();
    }

    private boolean checkDuplicateEntities(String address) {
        for (RequestPaymentHandler handler : dataList) {
            if (handler.getAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }
    
    private void getSelectedItemsFromTable() {
        List<String> result = getCurrentDate();
        txtAccount.setText(result.get(0));
        txtAddress.setText(result.get(1));
        txtBTC.setText(result.get(2));
        Double fiatValue = (Double.parseDouble(result.get(2)) * BitcoinCurrencyRateApi.currentRate.getValue());
        txtFiat.setText(String.format("%.2f", fiatValue));
        cmbRequestExpires.setSelectedItem(result.get(3));
        txtDescription.setText(result.get(4));
        address = result.get(1);
        btcAmount = result.get(2);
        description = result.get(4);
        loadQRCode(txtAddress.getText(), btcAmount, description.replaceAll("[\\s|\\u00A0]+", ""));
    }

    private List<String> getCurrentDate() {
        for (RequestPaymentHandler handler : dataList) {
            if (handler.getDate() != null && handler.getDate().equals(date)) {
                return Arrays.asList(handler.getAccount(), handler.getAddress(), handler.getAmount(), handler.getExpireTime(), handler.getDescription());
            }
        }
        return null;
    }

    private void populateJTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        for (RequestPaymentHandler listComponent : dataList) {
            model.addRow(new Object[]{
                listComponent.getDate(),
                listComponent.getDescription(),
                listComponent.getAmount(),
                listComponent.getStatus()
            });
        }
        
        try {
            for (int y = 0 ; y < jTable1.getRowCount() ; y++){
                model.moveRow(model.getRowCount() - 1, model.getRowCount() - 1, y);
            }
        } catch(Exception e){}
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int x = 0; x < model.getColumnCount(); x++) {
            jTable1.getColumnModel().getColumn(x).setCellRenderer(rightRenderer);
        }
    }

    private boolean checkStatusLifeCycle() {
        try {
            int modifiedIndex = (jTable1.getRowCount() - (jTable1.getSelectedRow() + 1));
            String receiveStatus = getStatus(modifiedIndex);
            if (receiveStatus.equals("Expired")) {
                try {
                    String deleteRow = jTable1.getModel().getValueAt(modifiedIndex, 0).toString();
                    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                    model.removeRow(modifiedIndex);
                    deleteXML(deleteRow);                    
                } catch (ArrayIndexOutOfBoundsException e) { }
                return true;
            }
            else if (receiveStatus.equals("Paid")) {
                comboEfficient("Request Paid");
            }
            else if (receiveStatus.equals("pending")) {
                //Fetching Selected Request Date And Time
                String requestDateTimeString = getDate(modifiedIndex);
                //Fetching Current Date and Time
                Date currentDateTime = new Date();
                Date tableDateTime = null;
                
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    tableDateTime = format.parse(requestDateTimeString);
                } catch (ParseException ex) {
                    System.out.println("-------Exception Occured in Parsing Formats of Dates in PnlNewRequestPaymentScreen.java------");
                }
                try {
                    long diff = currentDateTime.getTime() - tableDateTime.getTime();
                    long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = diff / (60 * 1000) % 60;
                    long diffHours = diff / (60 * 60 * 1000) % 24;
                    long diffDays = diff / (24 * 60 * 60 * 1000);
                    String requestExpireTime = getExpireTime(modifiedIndex);

                    if (requestExpireTime.equals("1 Hour")) {
                        if (diffDays == 0 && diffHours == 0) {
                            if (diffSeconds > 0 && diffMinutes < 60) {
                                long Minutes = 60 - diffMinutes;
                                comboEfficient("Around " + Minutes + " Minutes Left");
                            } 
                        }
                    }
                    else if (requestExpireTime.equals("1 Day")) {
                        if (diffDays == 0) {
                            if (diffSeconds > 0 && diffHours < 24) {
                                long Hours = 23 - diffHours;
                                long Minutes = 60 - diffMinutes;
                                comboEfficient("Around " + Hours + " Hours And " + Minutes + " Minutes Left");
                            }
                        }
                    }
                    else if (requestExpireTime.equals("1 Week")) {
                        if (diffSeconds > 0 && diffDays < 7) {
                            long Days = 6 - diffDays;
                            long Hours = 23 - diffHours;
                            comboEfficient("Around " + Days + " Days, And " + Hours + " Hours Left");
                        }
                    }
                    else if (requestExpireTime.equals("1 Month")) {
                        Date date = tableDateTime;
                        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        int month = localDate.getMonthValue();
                        if (diffSeconds > 0 && diffDays < 30) {
                            if (month == 2) {
                                int year = localDate.getYear();
                                if (year % 4 == 0) {
                                    long Days = 28 - diffDays;
                                    long Hours = 23 - diffHours;
                                    comboEfficient("Around " + Days + " Days, And " + Hours + " Hours Left");
                                } else {
                                    long Days = 27 - diffDays;
                                    long Hours = 23 - diffHours;
                                    comboEfficient("Around " + Days + " Days, And " + Hours + " Hours Left");
                                }
                            } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                                long Days = 30 - diffDays;
                                long Hours = 23 - diffHours;
                                comboEfficient("Around " + Days + " Days, And " + Hours + " Hours Left");
                            } else {
                                long Days = 29 - diffDays;
                                long Hours = 23 - diffHours;
                                comboEfficient("Around " + Days + " Days, And " + Hours + " Hours Left");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("-------Exception Occured in Dates in PnlNewRequestPaymentScreen.java------");
                }
            }
        }
        catch (Exception e) {}
        return false;
    }
    
    private void comboEfficient(String Message) {
        jComboBox1.removeAllItems();
        jComboBox1.addItem(Message);
        jComboBox1.setSelectedItem(Message);
        cmbRequestExpires.setVisible(false);
        jComboBox1.setVisible(true);
    }

    private String getDate(int Index) {
        try {
            RequestPaymentHandler rawData = dataList.get(Index);
            String sentDate = rawData.getDate();
            return sentDate;
        } catch (ArrayIndexOutOfBoundsException exception) { }
        return null;
    }

    private String getExpireTime(int Index) {
        try {
            RequestPaymentHandler rawData = dataList.get(Index);
            String sentExpireTime = rawData.getExpireTime();
            return sentExpireTime;
        } catch (ArrayIndexOutOfBoundsException exception) { }
        return null;
    }

    private void customizeUI() {
        themeWalletActionButton(btnSaveRecord, ResourcesProvider.Colors.NAV_MENU_WALLET_COLOR);
        themeWalletActionButton(btnNewRecord, ResourcesProvider.Colors.NAV_MENU_DASHBOARD_COLOR);
        txtAccount.setText("" + PnlWalletScreen.cmbWallets.getSelectedItem());
        jTable1.getTableHeader().setFont(ResourcesProvider.Fonts.DEFAULT_HEADING_FONT);
        jTable1.getTableHeader().setForeground(Color.BLACK);
        jTable1.getTableHeader().setBackground(Colors.TABLE_HEADER_BG_COLOR);
        jTable1.getTableHeader().setOpaque(true);
        jTable1.setFont(new Font("Tahoma", Font.PLAIN, 11));
    }

    private static void themeWalletActionButton(JButton button, Color background) {
        XButtonFactory
                .themedButton(button)
                .color(Color.WHITE)
                .background(background)
                .font(ResourcesProvider.Fonts.BOLD_SMALL_FONT);
    }
    
    private String getStatus(int Index) {
        String sentStatus = "";
        if (dataList.size() != 0) {
            RequestPaymentHandler rawData = dataList.get(Index);
            sentStatus = rawData.getStatus();
        }
        return sentStatus;
    }
    
    public static void gettingAddresses ()
    {
        try {
            addressList.clear();
            SAXBuilder builder = new SAXBuilder();
            document1 = (Document) builder.build(xmlFilePath);
            Element rootNode = document1.getRootElement();
            List list = rootNode.getChildren("Requests");

            for (int i = 0; i < list.size(); i++) {
                rawData1 = dataList.get(i);
                addressList.add(rawData1.getAddress());
            }
        }
        catch (JDOMException | IOException ex) { }
    }

    private void startTimerToUpdateStatus()
    {
        try {
            Timer timer = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    readDashboardScreenTable();
                }
            });
            timer.start();
        }
        catch(Exception exc){

        }
    }
    
    private void clearFields()
    {
        jComboBox1.setVisible(false);
        cmbRequestExpires.setVisible(true);
        cmbRequestExpires.setSelectedIndex(0);
        txtBTC.setText("");
        txtFiat.setText("");
        txtDescription.setText("");
        lblQrcode.setIcon(null);
        lblQrcode.setBackground(Color.WHITE);        
        loadQRCode(txtAddress.getText(), "0.00", "".replaceAll("[\\s|\\u00A0]+", ""));        
    }
    
    private void newForCheckExpireRequest()
    {
        int totalRows = jTable1.getRowCount();
        if(totalRows <= 0){ }
        else{
            try {
                for (int i = 0; i < totalRows; i++) 
                {
                    int modifiedIndex = (totalRows - (i + 1));
                    String selectedExpireTime = getExpireTime(modifiedIndex);
                    String requestTime = getRequestTime(modifiedIndex);
                    Date requestDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(requestTime);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(requestDate);

                    if (selectedExpireTime.equals("1 Hour")) {
                        cal.add(Calendar.HOUR_OF_DAY, 1);
                        Date compareDate = cal.getTime();
                        Date currentDate = new Date();
                        long dHourAdded = compareDate.getTime();
                        long dCurrentDate = currentDate.getTime();
                        if(dCurrentDate > dHourAdded){
                            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                            model.removeRow(i);
                            deleteXML(requestTime);
                        }
                    }
                    else if (selectedExpireTime.equals("1 Day")) {
                        cal.add(Calendar.HOUR_OF_DAY, 24); 
                        Date compareDate = cal.getTime();
                        Date currenDate = new Date();
                        long dHoursAdded = compareDate.getTime();
                        long dCurrentDate = currenDate.getTime();
                        if(dCurrentDate > dHoursAdded){
                            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                            model.removeRow(i);
                            deleteXML(requestTime);
                        }
                    }
                    else if (selectedExpireTime.equals("1 Week")) {
                        cal.add(Calendar.HOUR_OF_DAY, 168);
                        Date compareDate = cal.getTime();
                        Date curreDate = new Date();
                        long dHoursAdded = compareDate.getTime();
                        long dCurrentDate = curreDate.getTime();
                        if(dCurrentDate > dHoursAdded){
                            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                            model.removeRow(i);
                            deleteXML(requestTime);
                        }
                    }
                    else if (selectedExpireTime.equals("1 Month")) {
                        cal.add(Calendar.MONTH, 1);
                        Date compareDate = cal.getTime();
                        Date curreDate = new Date();
                        long dHoursAdded = compareDate.getTime();
                        long dCurrentDate = curreDate.getTime();
                        if(dCurrentDate > dHoursAdded){
                            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                            model.removeRow(i);
                            deleteXML(requestTime);
                        }
                    }
                }
            } 
            catch (Exception parseException) { }
        }
    }

    private void TimerToDeleteExpiredRequests()    {
        try {
            Timer timer = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newForCheckExpireRequest();
                }
            });
            timer.start();
        } catch(Exception exc){ }
    }

    private String getRequestTime(int Index) {
        RequestPaymentHandler rawData = dataList.get(Index);
        String time = rawData.getDate();
        return time;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewRecord;
    private javax.swing.JButton btnSaveRecord;
    private javax.swing.JComboBox cmbRequestExpires;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblAccount;
    private javax.swing.JLabel lblAmount;
    private javax.swing.JLabel lblBTC;
    private javax.swing.JLabel lblFiat;
    private javax.swing.JLabel lblQrcode;
    private javax.swing.JLabel lblReceiverAddress;
    private javax.swing.JLabel lblRequestExpires;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JLabel txtAccount;
    public static javax.swing.JLabel txtAddress;
    public javax.swing.JTextField txtBTC;
    public javax.swing.JTextArea txtDescription;
    public javax.swing.JTextField txtFiat;
    // End of variables declaration//GEN-END:variables
}