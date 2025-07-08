package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.model.AcaoPendente;
import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.util.DialogUtil;
import br.com.carlos.bibliotecafx.util.FilaSincronizacao;
import br.com.carlos.bibliotecafx.util.GerenciadorDadosLocal;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TelaEdicaoLivroController {

    @FXML
    private CheckBox checkLido;
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAutor;
    @FXML
    private TextField txtAno;
    @FXML
    private TextField txtPaginas;
    @FXML
    private ImageView imgCapa;
    @FXML
    private Button btnCarregarCapa;
    @FXML
    private TextArea txtSinopse;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnCancelar;

    private LivroFx livroAtual;
    private ObservableList<LivroFx> listaPrincipalDeLivros; // Referência à lista da tela principal
    private byte[] novaCapaBytes;
    private Runnable onEdicaoConcluidaCallback;

    /**
     * Método de inicialização modificado. Agora recebe também a lista principal de livros.
     *
     * @param livro         O livro a ser editado, ou um novo livro vazio.
     * @param todosOsLivros A lista principal da aplicação.
     */
    public void inicializarDados(LivroFx livro, ObservableList<LivroFx> todosOsLivros) {
        this.livroAtual = livro;
        this.listaPrincipalDeLivros = todosOsLivros;
        this.novaCapaBytes = livro.getCapa();

        txtTitulo.setText(livro.getTitulo());
        txtAutor.setText(livro.getAutor());
        txtAno.setText(livro.getAno() == 0 ? "" : String.valueOf(livro.getAno()));
        txtPaginas.setText(livro.getNumeroPaginas() == 0 ? "" : String.valueOf(livro.getNumeroPaginas()));
        txtSinopse.setText(livro.getSinopse());
        checkLido.setSelected(livro.isLido());

        if (livro.getCapa() != null && livro.getCapa().length > 0) {
            imgCapa.setImage(new Image(new ByteArrayInputStream(livro.getCapa())));
        } else {
            imgCapa.setImage(null);
        }
    }

    public void setOnEdicaoConcluidaCallback(Runnable callback) {
        this.onEdicaoConcluidaCallback = callback;
    }

    @FXML
    private void carregarNovaCapa() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione uma Imagem de Capa");
        fileChooser.getExtensionFilters()
                   .addAll(
                           new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.jpeg", "*.gif"),
                           new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*")
                   );
        File arquivoSelecionado = fileChooser.showOpenDialog(btnCarregarCapa.getScene()
                                                                            .getWindow());
        if (arquivoSelecionado != null) {
            try {
                this.novaCapaBytes = Files.readAllBytes(arquivoSelecionado.toPath());
                imgCapa.setImage(new Image(new ByteArrayInputStream(this.novaCapaBytes)));
            } catch (IOException e) {
                DialogUtil.showError("Erro de Arquivo", "Não foi possível ler a imagem selecionada.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Lógica de salvamento refatorada para o modo "Offline-First".
     * A ação agora é instantânea e local.
     */
    @FXML
    private void salvarAlteracoes() {
        if (!validarCampos()) {
            return;
        }

        boolean isNewBook = (livroAtual.getId() == null || livroAtual.getId() == 0);

        // Atualiza o objeto com os dados dos campos do formulário
        livroAtual.setTitulo(txtTitulo.getText());
        livroAtual.setAutor(txtAutor.getText());
        livroAtual.setAno(txtAno.getText()
                                .trim()
                                .isEmpty() ? 0 : Integer.parseInt(txtAno.getText()
                                                                        .trim()));
        livroAtual.setNumeroPaginas(txtPaginas.getText()
                                              .trim()
                                              .isEmpty() ? 0 : Integer.parseInt(txtPaginas.getText()
                                                                                          .trim()));
        livroAtual.setSinopse(txtSinopse.getText());
        livroAtual.setCapa(this.novaCapaBytes);
        livroAtual.setLido(checkLido.isSelected());

        AcaoPendente acao;
        if (isNewBook) {
            // Se é um livro novo, gera um ID local temporário e o adiciona à lista principal
            livroAtual.setId(-System.currentTimeMillis());
            listaPrincipalDeLivros.add(livroAtual);
            acao = new AcaoPendente("ADD", null, livroAtual);
            System.out.println("Novo livro adicionado localmente com ID temporário: " + livroAtual.getId());
        } else {
            // Se é uma edição, a lista principal já é atualizada automaticamente
            // pois 'livroAtual' é uma referência a um objeto que já está na lista.
            acao = new AcaoPendente("UPDATE", livroAtual.getId(), livroAtual);
            System.out.println("Livro ID " + livroAtual.getId() + " atualizado localmente.");
        }

        // Adiciona a tarefa na fila de sincronização
        FilaSincronizacao.adicionarOuAtualizarAcao(acao);

        // Salva a lista inteira (com o novo item ou com o item atualizado) no arquivo local
        GerenciadorDadosLocal.salvarBiblioteca(listaPrincipalDeLivros);

        // Chama o callback para que a tela principal possa se recarregar do arquivo local
        if (onEdicaoConcluidaCallback != null) {
            onEdicaoConcluidaCallback.run();
        }

        DialogUtil.showSuccess("Salvo!", "As alterações foram salvas localmente e serão sincronizadas em breve.");
        cancelarEdicao(); // Fecha a janela
    }

    private boolean validarCampos() {
        String titulo = txtTitulo.getText();
        if (titulo == null || titulo.trim()
                                    .isEmpty()) {
            DialogUtil.showError("Erro de Validação", "O campo 'Título' é obrigatório.");
            return false;
        }
        try {
            if (!txtAno.getText()
                       .trim()
                       .isEmpty()) Integer.parseInt(txtAno.getText()
                                                          .trim());
        } catch (NumberFormatException e) {
            DialogUtil.showError("Erro de Validação", "O campo 'Ano' deve ser um número válido.");
            return false;
        }
        try {
            if (!txtPaginas.getText()
                           .trim()
                           .isEmpty()) Integer.parseInt(txtPaginas.getText()
                                                                  .trim());
        } catch (NumberFormatException e) {
            DialogUtil.showError("Erro de Validação", "O campo 'Páginas' deve ser um número válido.");
            return false;
        }
        return true;
    }

    @FXML
    private void cancelarEdicao() {
        Stage stage = (Stage) btnCancelar.getScene()
                                         .getWindow();
        stage.close();
    }
}