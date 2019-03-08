package redeneuralmlp;

public class NeuronioEntrada {
    private String id;
    private double entrada;

    public NeuronioEntrada(String id) {
        this.id = id;
    }
    
    public NeuronioEntrada(String id, double entrada) {
        this.id = id;
        this.entrada = entrada;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getEntrada() {
        return entrada;
    }

    public void setEntrada(double entrada) {
        this.entrada = entrada;
    }

    @Override
    public String toString() {
        return id + " =  " + entrada;
    }
    
    
}
