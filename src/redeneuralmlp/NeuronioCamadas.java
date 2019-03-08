package redeneuralmlp;

public class NeuronioCamadas {
    private String id;
    private double net;
    private double i;
    private double erro;
    private double valEntrada;

    public NeuronioCamadas() {
    }

    public NeuronioCamadas(String id) {
        this.id = id;
    }

    public NeuronioCamadas(String id, double net, double i, double erro, double valEntrada) {
        this.id = id;
        this.net = net;
        this.i = i;
        this.erro = erro;
        this.valEntrada = valEntrada;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getNet() {
        return net;
    }

    public void setNet(double net) {
        this.net = net;
    }

    public double getI() {
        return i;
    }

    public void setI(double i) {
        this.i = i;
    }

    public double getErro() {
        return erro;
    }

    public void setErro(double erro) {
        this.erro = erro;
    }

    public double getValEntrada() {
        return valEntrada;
    }

    public void setValEntrada(double valEntrada) {
        this.valEntrada = valEntrada;
    }

    @Override
    public String toString() {
        return id + ", net=" + net + ", i=" + i + ", erro=" + erro + ", ent=" + valEntrada;
    }
    
    
}
