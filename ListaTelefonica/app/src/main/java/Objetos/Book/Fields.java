package Objetos.Book;

import java.util.List;

public class Fields
{
    public int cid_codigo ;
    public String cid_uf ;
    public String cid_descricao ;

    public String ianuncio ;

    public String strtopo ;

    public int ati_codigo ;
    public String ati_descricao ;

    public int end_codigo ;
    public String end_bairro ;
    public String end_cep ;
    public String end_complemento ;
    public String end_numero ;
    public String end_completo ;
    public String end_intervalo ;

    public String cli_nome ;
    public String cli_telefone ;
    public String cli_operadora_telefone_1 ;
    public String cli_telefone_1 ;
    public String cli_operadora_telefone_2 ;
    public String cli_telefone_2 ;

    public long anu_codigo ;
    public String anu_conteudo ;
    public String anu_email ;
    public String new_latlon ;
    public String anu_site ;
    public int anu_mobile ;
    public String anu_tipo_mapa ;
    public int anu_exibir_banner ;
    public String anu_logo ;
    public String anu_pesquisa ;
    public String anu_url_facebook ;
    public String anu_url_twitter ;
    public String anu_detalhe_1 ;
    public String anu_detalhe_2 ;
    public String anu_data ;
    public String anu_data_fim ;
    public String anu_chamada ;
    public String anu_whatsapp ;
    public String anu_redirecionamento ;
    public String faceta_bairro;
    public String faceta_descricao;
    public String faceta_endereco;

    public List anu_imagens ;
    public List anu_relacionados ;

    public Double getLatitude()
    {
        if (new_latlon == null)
            return 0d;
        try {
            String[] position = new_latlon.split(",");
            return Double.parseDouble(position[0]);
        }
        catch (Exception e) {
            return 0d;
        }
    }

    public Double getLongitude()
    {
        if (new_latlon == null)
            return 0d;
        try {
            String[] position = new_latlon.split(",");
            return Double.parseDouble(position[1]);
        }
        catch (Exception e) {
            return 0d;
        }
    }
}
