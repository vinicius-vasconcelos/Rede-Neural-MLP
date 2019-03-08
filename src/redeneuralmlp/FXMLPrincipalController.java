package redeneuralmlp;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLPrincipalController implements Initializable {
    private double cx, cy;
    
    private double max, min; // variaveis para normalizar entrada
    private ArrayList<Double> Amax;
    private ArrayList<Double> Amin;
    public FXMLPrincipalController() {
    }
    private int numEntradas; // numero de neuronios na camada de entrada
    
    private ArrayList<String> entArqMem; // array de dados trazidos dos arquivo
    private ArrayList<NeuronioEntrada> entradas;
    private ArrayList<NeuronioCamadas> camadaOculta;
    private ArrayList<NeuronioCamadas> saidas;
    
    private ArrayList<Double> erros;
    private ArrayList<Double> errosEpocas;
    
    private double[][] matPesosOcul; //matriz de pesos ocultos sorteados
    private double[][] matPesosSai; // matriz de pesos de saida sorteados
    private int[][] matIdent; //matriz de saidas desejadas
    private int[][] matConfusao; //matriz de respostas
    
    private RandomAccessFile arquivo;
    
    @FXML
    private BorderPane pnPrincipal;
    @FXML
    private AnchorPane pnMenu;
    @FXML
    private JFXButton btLerArquivo;
    @FXML
    private JFXButton btLimpar;
    @FXML
    private JFXTextField tfCamadaEntrada;
    @FXML
    private JFXTextField tfCamadaSaida;
    @FXML
    private JFXTextField tfCamadaOculta;
    @FXML
    private JFXTextField tfErro;
    @FXML
    private JFXTextField tfInteracoes;
    @FXML
    private JFXTextField tfN;
    @FXML
    private JFXRadioButton rbLinear;
    @FXML
    private JFXRadioButton rbLogistica;
    @FXML
    private JFXRadioButton rbHiperbolica;
    @FXML
    private JFXCheckBox cbBaias;
    @FXML
    private JFXButton btFechar;
    @FXML
    private JFXButton btRealizarTreinamento;
    @FXML
    private JFXButton btRealizarTestes;
    @FXML
    private LineChart<?, ?> lcGrafico;
    @FXML
    private JFXTextArea taMatriz;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        limparTela();
    }    

    @FXML
    private void evtBtLerArquivo(ActionEvent event) {
        boolean flag = true;
        boolean baias = cbBaias.isSelected();
        String ler = "";
        String nome = "teste";
        
        FileChooser fc  = new FileChooser(); //abrir janela
        File caminho = fc.showOpenDialog(null); // caminho*/
      
        if(caminho != null) {
            
            try {
                arquivo =  new RandomAccessFile(caminho.getAbsoluteFile(), "r"); // abrindo arquivo

                ler = arquivo.readLine();
                
                while(ler != null){
                    
                    String[] linha = ler.split(",");
                    
                    if(flag) {
                        numEntradas = (linha.length - 1); // quantos neuronios de enrada existem X1, X2, X3, .....

                        for (int i = 0; i < numEntradas; i++) // gerando neuronios de entrada
                                entradas.add(new NeuronioEntrada(linha[i]));
                        flag = false;
                    }
                    else {
                        entArqMem.add(ler); //jogando arquivo em memória

                        //obtendo neuronios da camada de saida
                        String classe = linha[numEntradas];
                        if (!classe.equals(nome)) { 
                            nome = classe;
                            saidas.add(new NeuronioCamadas("S" + nome));
                        }
                    }
                    ler = arquivo.readLine();
                }
                
            } catch (IOException e) {
                System.out.println("Arquivo não pode ser aberto !!!!" + "\n" + e.getMessage());
            }
            // se baias tiver habilitado criar um novo neuronio com entrada valendo 1
            if(baias)
                preencheBaias();
            
            // sugestão de neuronios na camada oculta = (entradas + saidas) / 2
            sugestaoOculta();
            
            for (int i = 0; i < numEntradas; i++) {
                // normalizando valores do arquivo pegando max e min
                pesquisaMinMax(i);
            }
            
            btRealizarTreinamento.setDisable(false);
            btLerArquivo.setDisable(true);
            tfCamadaEntrada.setText("" + numEntradas);
            tfCamadaSaida.setText("" + saidas.size());
        }
    }
    
    @FXML
    private void evtBtLimpar(ActionEvent event) {
        limparTela();
    }

    @FXML
    private void evtRbLinear(ActionEvent event) {
        rbLinear.setSelected(true);
        rbLogistica.setSelected(false);
        rbHiperbolica.setSelected(false);
    }

    @FXML
    private void evtRbLogistica(ActionEvent event) {
        rbLinear.setSelected(false);
        rbLogistica.setSelected(true);
        rbHiperbolica.setSelected(false);
    }

    @FXML
    private void evtRbHiperbolica(ActionEvent event) {
        rbLinear.setSelected(false);
        rbLogistica.setSelected(false);
        rbHiperbolica.setSelected(true);
    }
   
    @FXML
    private void evtBtFechar(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void evtBtRealizarTreinamento(ActionEvent event) {
        boolean fim = false;
        int epocas = 0;
        // gerando neuronios de camada oculta
        geraNeurCamOculta();
        //gerando matriz de pesos ocultos (0 - inicializar matrizes de pesos)
        matrizPesosOcultos();
        matrizPesosSaidas();
        matrizIdentidade();
        btRealizarTreinamento.setDisable(true);
        btRealizarTestes.setDisable(false);
        
        String teste[];
        
        do {
           for (int i = 0; i < entArqMem.size(); i++) { // pecorrer todas as entradas dos testes
                teste = entArqMem.get(i).split(","); // obtendo a entradas
                
                for (int j = 0; j < teste.length - 1; j++) { // percorrendo vetor das entradas
                    // normalizando entrada para realizar testes (atualizando array de entradas)
                    entradas.get(j).setEntrada(((Double.parseDouble(teste[j]) - Amin.get(j)) / (Amax.get(j) - Amin.get(j)))); //(1 - aplicar entradas, normalizadas)
                }
               
                // (1 - calcular os NETs das camadas ocultas)
                calcularNetOculta();
               
                // (2 - aplicar função de transferncia nas camadas ocultas  achando o 'i' = f(net))
                aplicandoFuncoes(false, true, "");

                //(3 - calcular os nets da camada de saida i(oculta) * peso + i*(oculta) * peso ...)
                calcularNetSaida();

                // (4 - calculo do 'i' da camada de saida i = f(net)))
                aplicandoFuncoes(false, false, "");

                // (5 - calculo do erro da camada de saida erro = (desejado -  i * f'(net)))
                aplicandoFuncoes(true, false, teste[teste.length - 1]);

                // (6 - calculo do erro da REDE)
                erroRede();
                
                // (7 - calculo do erro da camada oculta erro = arrayErros * matPesosSaida * f'(net) )
                aplicandoFuncoes(true, true, "");
                
                //(8 - atualizando pesos da camada de saida)
                atualizaPesosSaida();

                //(9 - atualizando pesos da camda oculta)
                atualizaPesosOculta();
            }
           
            //ao final do arquivo somar erros, dividir, salvar epoca e verificar se convergiu a rede
            fim  = salvarEpoca();
            epocas++;
        }while(!fim && epocas < Integer.parseInt(tfInteracoes.getText())); //repeticão de convergencia ou epocas
      
        plotarGrafico(); // desenhando erros no grafico
    }

    @FXML
    private void evtBtRealizarTestes(ActionEvent event) {   
        //limpando arrays de entradas e entArqMem
        entArqMem = new ArrayList();
        entradas =  new ArrayList();
        
        btLerArquivo.setDisable(true);
        btRealizarTestes.setDisable(true);
        btRealizarTreinamento.setDisable(true);
        
        boolean flag = true;
        String ler = "";
        
        FileChooser fc  = new FileChooser(); //abrir janela
        File caminho = fc.showOpenDialog(null); // caminho
      
        if(caminho != null) {
            try {
                arquivo =  new RandomAccessFile(caminho.getAbsoluteFile(), "r"); // abrindo arquivo

                ler = arquivo.readLine();
                
                while(ler != null){
                    
                    String[] linha = ler.split(",");
                    
                    if(flag){
                        if(cbBaias.isSelected()) 
                            numEntradas--;
                        for (int i = 0; i < numEntradas; i++) // gerando neuronios de entrada
                            entradas.add(new NeuronioEntrada(linha[i]));
                        flag = false;
                    }
                    else
                        entArqMem.add(ler); //jogando arquivo em memória
                    
                    ler = arquivo.readLine();
                }
                // se baias tiver habilitado criar um novo neuronio com entrada valendo 1
                if(cbBaias.isSelected()) 
                    entradas.add(new NeuronioEntrada("bias", 1.0));
                
                aplicandoTestes(); // percorrer array de testes e faz os calculos
                
            } catch (IOException e) {
                System.out.println("Arquivo não pode ser aberto !!!!" + "\n" + e.getMessage());
            }
        }
    }
    
    @FXML
    private void evtDragged(MouseEvent event) {
        Stage stage = ((Stage) ((Node) event.getSource()).getScene().getWindow());
        stage.setX(event.getScreenX() + cx);
        stage.setY(event.getScreenY() + cy);
    }

    @FXML
    private void evtPressed(MouseEvent event) {
        Stage stage = ((Stage) ((Node) event.getSource()).getScene().getWindow());
        cx = stage.getX() - event.getScreenX();
        cy = stage.getY() - event.getScreenY();
    }
    
    private void funcaoLinear(boolean derivada, boolean oculta, String dese) { //aplica função linar ou sua derivada (3 - aplicar funções de transferencia na camada oculta ou saida)
        double somatorio = 0;
        int desejado  =  0;
        if(!dese.trim().equals(""))
            desejado  =  Integer.parseInt(dese);
            
        
        if(derivada) { // calculo do ERRO
            if(oculta) { // camada oculta
                for (int i = 0; i <camadaOculta.size(); i++) {
                    if(!camadaOculta.get(i).getId().equalsIgnoreCase("bias")) {
                        for (int j = 0; j < saidas.size(); j++) {
                            somatorio += (saidas.get(j).getErro() * matPesosSai[j][i]) * (1.0/10.0);
                        }
                        camadaOculta.get(i).setErro(somatorio);
                        somatorio = 0;
                    }
                }
            }
            else { //camada de saida
                for (int i = 0; i < saidas.size(); i++)
                    saidas.get(i).setErro((matIdent[desejado-1][i] - saidas.get(i).getI()) * (1.0/10.0));
            }     
        }
        else { //calculo do 'i'
            if(oculta)  // camada oculta
               for (int i = 0; i < camadaOculta.size(); i++){
                   if(!camadaOculta.get(i).getId().equalsIgnoreCase("bias"))
                        camadaOculta.get(i).setI(camadaOculta.get(i).getNet() / 10.0);
               }
            else // camada de saida
                for (int i = 0; i < saidas.size(); i++) 
                   saidas.get(i).setI(saidas.get(i).getNet() / 10.0);
        }
    }
    
    private void funcaoLogistica(boolean derivada,  boolean oculta, String dese) { //aplica função logistica ou sua derivada (3 - aplicar funções de transferencia na camada oculta ou saida)
        double somatorio = 0;
        int desejado  =  0;
        if(!dese.trim().equals(""))
            desejado  =  Integer.parseInt(dese);
      
        if(derivada) // derivada da função (erro)
           if(oculta) //erro
               for (int i = 0; i < camadaOculta.size(); i++) {
                   if(!camadaOculta.get(i).getId().equalsIgnoreCase("bias")){
                        for (int j = 0; j < saidas.size(); j++) 
                            somatorio += (saidas.get(j).getErro() * matPesosSai[j][i]) * (camadaOculta.get(i).getI() * (1 - camadaOculta.get(i).getI()));   
                        camadaOculta.get(i).setErro(somatorio);
                        somatorio = 0;
                   }
                }
           else // percorre array dos neuronios de saida
               for (int i = 0; i < saidas.size(); i++)
                   saidas.get(i).setErro((matIdent[desejado-1][i] - saidas.get(i).getI()) * (saidas.get(i).getI() * (1 - saidas.get(i).getI())));
        else // função normal (i)
            if(oculta)  // percorre array de neuronios da camada oculta
               for (int i = 0; i < camadaOculta.size(); i++) {
                   if(!camadaOculta.get(i).getId().equalsIgnoreCase("bias"))
                        camadaOculta.get(i).setI(1 / (1 + Math.pow(Math.E, -(camadaOculta.get(i).getNet()))));
               }
           else  // percorre array dos neuronios de saida
                for (int i = 0; i < saidas.size(); i++) 
                   saidas.get(i).setI(1 / (1 + Math.pow(Math.E, -(saidas.get(i).getNet()))));
    }
    
    private void funcaoTangenteHiperbolica(boolean derivada,  boolean oculta, String dese) { //aplica função tan. hiperbolica ou sua derivada (3 - aplicar funções de transferencia na camada oculta ou saida)
        double somatorio = 0;
        int desejado  =  0;
        if(!dese.trim().equals(""))
            desejado  =  Integer.parseInt(dese);
        
        if(derivada)  // derivada da função (erro)
           if(oculta)  //erro
               for (int i = 0; i < camadaOculta.size(); i++) {
                   if(!camadaOculta.get(i).getId().equalsIgnoreCase("bias")) {
                        for (int j = 0; j < saidas.size(); j++)
                            somatorio += (saidas.get(j).getErro() * matPesosSai[j][i]) * (1 - Math.pow(camadaOculta.get(i).getI(), 2));
                        camadaOculta.get(i).setErro(somatorio);
                        somatorio = 0;
                   }
               }
           else  // saida
               for (int i = 0; i < saidas.size(); i++) 
                   saidas.get(i).setErro((matIdent[desejado-1][i] - saidas.get(i).getI()) * (1 - Math.pow(saidas.get(i).getI(), 2)));
        else // função normal (i)
            if(oculta)  // percorre array de neuronios da camada oculta
               for (int i = 0; i < camadaOculta.size(); i++){
                   if(!camadaOculta.get(i).getId().equalsIgnoreCase("bias"))
                        camadaOculta.get(i).setI((1 - Math.pow(Math.E, -2 * camadaOculta.get(i).getNet())) / (1 + Math.pow(Math.E, -2 * camadaOculta.get(i).getNet())));
                }
           else  // percorre array dos neuronios de saida
                for (int i = 0; i < saidas.size(); i++) 
                   saidas.get(i).setI((1 - Math.pow(Math.E, -2 * saidas.get(i).getNet())) / (1 + Math.pow(Math.E, -2 * saidas.get(i).getNet())));
    }
    
    
    private void aplicandoFuncoes(boolean derivada, boolean oculta, String desejado) { //qual das funcoes sera usada, se é para erro ou i
        if(rbLinear.isSelected())
            funcaoLinear(derivada, oculta, desejado);
        if(rbLogistica.isSelected())
            funcaoLogistica(derivada, oculta, desejado);
        if(rbHiperbolica.isSelected())
            funcaoTangenteHiperbolica(derivada, oculta, desejado);
    }
    
    private void erroRede() { // calcula erro da rede somando neuronios
        double resultado = 0;
        for (int i = 0; i < saidas.size(); i++)
           resultado += Math.pow(saidas.get(i).getErro(), 2);
        erros.add((double)resultado * 0.5);
    }
    
    private void limparTela() {
        //inicializando arrays
        entradas = new ArrayList();
        camadaOculta = new ArrayList();
        saidas = new ArrayList();
        entArqMem = new ArrayList();
        erros =  new ArrayList();
        errosEpocas =  new ArrayList();
        
        Amax = new ArrayList();
        Amin = new ArrayList();
        
        taMatriz.setText("");
        taMatriz.setEditable(false);
        lcGrafico.getData().clear();
        arquivo = null;
        
        btLerArquivo.setDisable(false);
        btRealizarTreinamento.setDisable(true);
        btRealizarTestes.setDisable(true);
        
        tfCamadaEntrada.setText("0");
        tfCamadaEntrada.setDisable(true);
        tfCamadaSaida.setText("0");
        tfCamadaSaida.setDisable(true);
        tfCamadaOculta.setText("0");
        
        tfErro.setText("0.00001");
        tfInteracoes.setText("1000");
        tfN.setText("0.02");
        
        rbLinear.setSelected(true);
        rbLogistica.setSelected(false);
        rbHiperbolica.setSelected(false);
        
        cbBaias.setSelected(false);
        
        tfCamadaOculta.requestFocus();
    }
    
    private void preencheBaias() { //seta valores 1 para o baias se tiver habilitado e 0 se não
       entradas.add(new NeuronioEntrada("bias", 1.0));
       numEntradas++;
    }
    
    private void sugestaoOculta() { // neuronios na camada oculta = (entradas + saidas) / 2
        int var =  (entradas.size() + saidas.size()) / 2;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Atenção");
        alert.setHeaderText("Arquivo lido com sucesso !!!\n A sugestão de camadas ocultas é de '" + var + "', mas isto pode ser alterado !!!");
        alert.showAndWait();
        tfCamadaOculta.setText("" + var);
    }
    
    private void geraNeurCamOculta() { //gera neuronios da camda oculta a partir da escolha do usuario
        for (int i = 0; i < Integer.parseInt(tfCamadaOculta.getText()); i++) //percorre até fim do tfCamadaoculta e gera neuronios
            camadaOculta.add(new NeuronioCamadas("N" + i)); 
        if(cbBaias.isSelected())
            camadaOculta.add(new NeuronioCamadas("bias", 0.0, 1.0, 0.0, 0.0));
    }
    
    private void matrizIdentidade() {
        matIdent = new int[saidas.size()][saidas.size()];
        for (int i = 0; i < saidas.size(); i++)
            for (int j = 0; j < saidas.size(); j++)
                if(i == j)
                    matIdent[i][j] = 1;
                else
                    matIdent[i][j] = 0;
    }
    
    private void matrizPesosOcultos() { // gera matriz de pesos ocultos e sorteia valores para ela entre -1 e 1
       int soma;
       double sort;
       
        if(cbBaias.isSelected())
            matPesosOcul = new double[Integer.parseInt(tfCamadaOculta.getText()) + 1][numEntradas];
        else
            matPesosOcul = new double[Integer.parseInt(tfCamadaOculta.getText())][numEntradas];
        
        for (int i = 0; i < Integer.parseInt(tfCamadaOculta.getText()); i++) {
            for (int j = 0; j < numEntradas; j++) {
                sort = Math.random() * 1;
                soma = (i+j) % 2;
                
                if(soma == 0)
                    sort = sort * -1;
                
                matPesosOcul[i][j] = sort;
            } 
        }
    }
    
    private void atualizaPesosOculta() { //(9 - atuzaliza pesos da matrizPesosOcultos = pesoAtual + N * erroneuronio * entrada)
        for (int i = 0; i < Integer.parseInt(tfCamadaOculta.getText()); i++)
            for (int j = 0; j < numEntradas; j++) 
                matPesosOcul[i][j] = matPesosOcul[i][j] + (Double.parseDouble(tfN.getText()) * camadaOculta.get(i).getErro() * entradas.get(j).getEntrada());
    }
    
    private void matrizPesosSaidas() { // gera matriz de pesos na saida e sorteia valores para ela entre -1 e 1
        int soma;
        double sort;
       
        if(cbBaias.isSelected())
            matPesosSai = new double[saidas.size()][Integer.parseInt(tfCamadaOculta.getText()) + 1];
        else
            matPesosSai = new double[saidas.size()][Integer.parseInt(tfCamadaOculta.getText())];
        
        for (int i = 0; i < saidas.size(); i++) {
            for (int j = 0; j < Integer.parseInt(tfCamadaOculta.getText()); j++) {
                sort = Math.random() * 1;
                soma = (i+j) % 2;
                
                if(soma == 0)
                    sort = sort * -1;
                
                matPesosSai[i][j] = sort;
            } 
        } 
    }
    
    private void atualizaPesosSaida() { //(8 - atuzaliza pesos da matrizPesosSaidas = pesoAtual + N * erroRede * i(camada oculta))
        for (int i = 0; i < saidas.size(); i++) 
            for (int j = 0; j < Integer.parseInt(tfCamadaOculta.getText()); j++)
                matPesosSai[i][j] = matPesosSai[i][j] + (Double.parseDouble(tfN.getText()) * saidas.get(i).getErro() * camadaOculta.get(j).getI());
    }
    
    private void matrizConfusao(){ // gerando matriz de confusão para testar saidas
        matConfusao =  new int[saidas.size()][saidas.size()];
        
        for (int i = 0; i < saidas.size(); i++)
            for (int j = 0; j < saidas.size(); j++)
                matConfusao[i][j] = 0;
    }
   
    private void pesquisaMinMax(int col) { // percorre array de dados lidos do arquivo e os normaliza
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        
        // achando max e min
        for (int i = 0; i < entArqMem.size(); i++) {
            String dado[] = entArqMem.get(i).split(",");
            
            if(Double.parseDouble(dado[col]) < min)
                min = Double.parseDouble(dado[col]);
            if(Double.parseDouble(dado[col]) > max)
                max = Double.parseDouble(dado[col]);   
        }
        
        Amax.add(max);
        Amin.add(min);
    }
    
    private void calcularNetOculta() { // entradas vezes pesos somando, achando o net dos neuronios da camda oculta (2 - calcular os NETs das camadas ocultas)
        //variavel para controlar matriz de pesos ocultos
        int l = 0;
        int c = 0;
        
        double net; // variavel somadora de net (X1 * p1 + X2 * p2 + ....)
        
        for (int i = 0; i < camadaOculta.size(); i++) { // percorrendo neuronios de camada oculta
            if(!camadaOculta.get(i).getId().equalsIgnoreCase("bias")){
                net = 0;
                for (int j = 0; j < entradas.size(); j++)  // percorrendo neuronios da camada de entrada
                    net += entradas.get(j).getEntrada() * matPesosOcul[l][c++];
                camadaOculta.get(i).setNet(net);// armazendo novo valor dor net
                l++;
                c = 0;
            }
        }
    }
    
    private void calcularNetSaida () { //i(oculta) * peso + i(oculta) * peso + .... (4 - calcular os nets da camada de saida)
        double net = 0;
        int l = 0;
        int c = 0;
        
        for (int i = 0; i < saidas.size(); i++) {
            net = 0;
            for (int j = 0; j < camadaOculta.size(); j++) 
                net += camadaOculta.get(j).getI() * matPesosSai[l][c++];
            saidas.get(i).setNet(net);
            l++;
            c = 0;
        }
    }
    
    private boolean salvarEpoca() { // verifica se rede convergiu e salva erro no array de epocas
        int i = 0;
        double total = 0;
        
        for (i = 0; i < erros.size(); i++) //somando erros
            total += erros.get(i);
        errosEpocas.add(total / i); //salvando em array de epocas
        erros.clear(); // limpando erros para proxima epoca
       
        if(errosEpocas.get(errosEpocas.size() - 1) < Double.parseDouble(tfErro.getText())) //verificando se convergiu
            return true;
        return false;
    }
    
    private void plotarGrafico() {
        lcGrafico.getData().clear();
        XYChart.Series serie = new XYChart.Series();
        
        for (int i = 0; i < errosEpocas.size(); i++)
            serie.getData().add(new XYChart.Data(i+1 + "",errosEpocas.get(i)));
        
        lcGrafico.setLegendVisible(false);
        lcGrafico.setCreateSymbols(false);
        
        lcGrafico.getData().add(serie);
    }
    
    private void aplicandoTestes() { //percorre array de teste entArqMem realizando os calculos
        matrizConfusao(); //inicializa zerando matriz de confusão
        
        String teste[];
        for (int i = 0; i < entArqMem.size(); i++) { // pecorrer todas as entradas dos testes
            teste = entArqMem.get(i).split(","); // obtendo a entradas
            for (int j = 0; j < teste.length - 1; j++) { // percorrendo vetor das entradas
                // normalizando entrada para realizar testes (atualizando array de entradas)
                entradas.get(j).setEntrada(((Double.parseDouble(teste[j]) - Amin.get(j)) / (Amax.get(j) - Amin.get(j)))); //(1 - aplicar entradas, normalizadas)
            }

            // (1 - calcular os NETs das camadas ocultas)
            calcularNetOculta();

            // (2 - aplicar função de transferncia nas camadas ocultas  achando o 'i' = f(net))
            aplicandoFuncoes(false, true, "");

            //(3 - calcular os nets da camada de saida i(oculta) * peso + i*(oculta) * peso ...)
            calcularNetSaida();

            // (4 - calculo do 'i' da camada de saida i = f(net)))
            aplicandoFuncoes(false, false, "");
            
            // realizar teste de saida e fazer classificação
            classificar(teste[teste.length - 1]);
        }
        
        plotarMatConfusao(); // mostrar matriz na tela
    }
    
    private void classificar(String val) { // realiza teste nas saidas e faz classificação, montando matriz de confusão
        int classe = Integer.parseInt(val); //classe do arquivo
        int resposta = 0;
        double maiorSaida = -1000.0;
        
        for (int i = 0; i < saidas.size(); i++) { //percorrendo saidas para verificar qual é a maior
            if(saidas.get(i).getI() > maiorSaida) {
                maiorSaida = saidas.get(i).getI();
                resposta = i;
            }
        }
       
        matConfusao[classe-1][resposta] = matConfusao[classe-1][resposta] + 1;
    }
    
    private void plotarMatConfusao() { // mostrar matriz de confusão na tela
        
        for (int i = 0; i < saidas.size(); i++) {
            for (int j = 0; j < saidas.size(); j++) {
                taMatriz.setText(taMatriz.getText() + " \t " + matConfusao[i][j]);
            }
            taMatriz.setText(taMatriz.getText() + "\n\n");

        }
    }
   
            
        
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void exibeErro() {
        for (int i = 0; i < erros.size(); i++) 
            System.out.println(erros.get(i));
    }
    
    private void exibeErroEpoca() {
        for (int i = 0; i < errosEpocas.size(); i++) 
            System.out.println(errosEpocas.get(i));
    }
    
    private void exibeDadosMemo() {
        for (int i = 0; i < entArqMem.size(); i++)
            System.out.println(entArqMem.get(i));
    }
    
    private void exibeEntradas() {
        for (int i = 0; i < entradas.size(); i++)
            System.out.println(entradas.get(i));
    }
    
    private void exibeCamadaOculta() {
        for (int i = 0; i < camadaOculta.size(); i++)
            System.out.println(camadaOculta.get(i));
    }
    
    private void exibeSaidas() {
        for (int i = 0; i < saidas.size(); i++)
            System.out.println(saidas.get(i));
    }
    
    private void exibeMatPesosOculta() {
        for (int i = 0; i < Integer.parseInt(tfCamadaOculta.getText()); i++) { 
            System.out.println("\n");
            for (int j = 0; j < numEntradas; j++) 
                System.out.print("\t" + matPesosOcul[i][j]);
        }
    }
    
    private void exibeMatPesosSaida() {
        for (int i = 0; i < saidas.size(); i++) {
            System.out.println("\n");
            for (int j = 0; j < Integer.parseInt(tfCamadaOculta.getText()); j++)
                System.out.print("\t" + matPesosSai[i][j]);
        }
    }
    
    private void exibeMatIdentidade() {
        for (int i = 0; i < saidas.size(); i++) {
            System.out.println("\n");
            for (int j = 0; j < saidas.size(); j++)
                System.out.print("\t" + matIdent[i][j]);
        }
    }
    
    private void exibeMatConfusao() {
        for (int i = 0; i < saidas.size(); i++) {
            System.out.println("\n");
            for (int j = 0; j < saidas.size(); j++)
                System.out.print(matConfusao[i][j]);
        }
    }
}
