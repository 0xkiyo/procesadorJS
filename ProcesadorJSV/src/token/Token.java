package token;


public class Token {

    private String id;
    private String valor;

    public Token(String id, String valor) {
        this.id = id;
        this.valor = valor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        String toReturn = "< ";
        if (this.getId() != null) {
            toReturn += this.getId() + " , ";
        }
        if (this.getValor() != null) {
            if ("CADENA".equals(this.getId())) {
                toReturn += "\"" + this.getValor() + "\" >";
            } else {
                toReturn += this.getValor() + " >";
            }
        } else {
            toReturn += "_ >";
        }
        return toReturn;
    }

    public boolean equals(Token token) {
        if (this.getId()!=null && !"PR".equals(this.getId())) {
            return this.getId().equals(token.getId());
        } else {//Estamos ante una palabra reservada
            if (this.getId() != null && this.getValor() != null) {
                return this.getId().equals(token.getId()) && this.getValor().equals(token.getValor());
            }else{
                return false;
            }
        }
    }

}