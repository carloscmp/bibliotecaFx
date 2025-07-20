package br.com.carlos.bibliotecafx.controller;

import br.com.carlos.bibliotecafx.model.AcaoPendente;
import br.com.carlos.bibliotecafx.model.LivroFx;
import br.com.carlos.bibliotecafx.persistence.FilaSincronizacao;
import br.com.carlos.bibliotecafx.persistence.GerenciadorDadosLocal;
import br.com.carlos.bibliotecafx.ui.DialogUtil;
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

/**
 * Controller para a janela de edição e cadastro de livros.
 * Lida com a entrada de dados do utilizador e guarda as alterações localmente.
 */
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
     * Inicializa a tela com os dados de um livro.
     * Se um livro novo e vazio é passado, a tela funciona em modo de "criação".
     * Se um livro existente é passado, funciona em modo de "edição".
     *
     * @param livro         O livro a ser editado, ou um novo objeto LivroFx para cadastro.
     * @param todosOsLivros A referência à lista de livros principal da aplicação.
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

    /**
     * Define uma ação a ser executada quando a edição for concluída com sucesso.
     *
     * @param callback A ação (geralmente para recarregar a lista principal).
     */
    public void setOnEdicaoConcluidaCallback(Runnable callback) {
        this.onEdicaoConcluidaCallback = callback;
    }

    /**
     * Abre um seletor de ficheiros para o utilizador escolher uma nova imagem de capa.
     */
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
                DialogUtil.showError("Erro de Ficheiro", "Não foi possível ler a imagem selecionada.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Lógica de salvamento refatorada para o modo "Offline-First".
     * A ação agora é instantânea, guardando localmente e adicionando à fila de sincronização.
     */
    @FXML
    private void salvarAlteracoes() {
        if (!validarCampos()) {
            return;
        }

        boolean isNewBook = (livroAtual.getId() == null || livroAtual.getId() == 0);

        // <<< CORREÇÃO: VERIFICAÇÃO DE DUPLICADOS ANTES DE SALVAR >>>
        if (isNewBook) {
            String novoTitulo = txtTitulo.getText()
                                         .trim();
            String novoAutor = txtAutor.getText()
                                       .trim();

            // Usa um stream para verificar se algum livro na lista principal já tem o mesmo título e autor (ignorando maiúsculas/minúsculas).
            boolean isDuplicate = listaPrincipalDeLivros.stream()
                                                        .anyMatch(livro ->
                                                                livro.getTitulo()
                                                                     .trim()
                                                                     .equalsIgnoreCase(novoTitulo) &&
                                                                        livro.getAutor()
                                                                             .trim()
                                                                             .equalsIgnoreCase(novoAutor)
                                                        );

            if (isDuplicate) {
                DialogUtil.showWarning("Livro Duplicado", "Um livro com o mesmo título e autor já existe na sua biblioteca.");
                return; // Interrompe a operação de salvamento
            }
        }

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
            acao = new AcaoPendente("UPDATE", livroAtual.getId(), livroAtual);
            System.out.println("Livro ID " + livroAtual.getId() + " atualizado localmente.");
        }

        FilaSincronizacao.adicionarOuAtualizarAcao(acao);

        GerenciadorDadosLocal.salvarBiblioteca(listaPrincipalDeLivros);

        if (onEdicaoConcluidaCallback != null) {
            onEdicaoConcluidaCallback.run();
        }

        DialogUtil.showSuccess("Salvo!", "As alterações foram guardadas localmente e serão sincronizadas em breve.");
        cancelarEdicao(); // Fecha a janela
    }

    /**
     * Valida os campos obrigatórios do formulário.
     *
     * @return true se os campos forem válidos, false caso contrário.
     */
    private boolean validarCampos() {
        String titulo = txtTitulo.getText();
        String autor = txtAutor.getText(); // Adicionado para validação

        if (titulo == null || titulo.trim()
                                    .isEmpty()) {
            DialogUtil.showError("Erro de Validação", "O campo 'Título' é obrigatório.");
            return false;
        }
        if (autor == null || autor.trim()
                                  .isEmpty()) { // Adicionada validação para o autor
            DialogUtil.showError("Erro de Validação", "O campo 'Autor' é obrigatório.");
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

    /**
     * Fecha a janela de edição/cadastro.
     */
    @FXML
    private void cancelarEdicao() {
        Stage stage = (Stage) btnCancelar.getScene()
                                         .getWindow();
        stage.close();
    }
}