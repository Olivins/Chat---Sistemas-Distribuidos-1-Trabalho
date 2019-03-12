package projetochat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Servidor extends Thread {
    

    private static Map MAP_CLIENTES;
    private Socket conexao;
    private String nomeCliente;
    private static List LISTA_DE_NOMES = new ArrayList();

    public Servidor(Socket socket) {
        this.conexao = socket;
    }

    public boolean armazena(String newName) {
        for (int i = 0; i < LISTA_DE_NOMES.size(); i++) {
            if (LISTA_DE_NOMES.get(i).equals(newName)) {
                return true;
            }
        }
        LISTA_DE_NOMES.add(newName);
        return false;
    }

    public void remove(String oldName) {
        for (int i = 0; i < LISTA_DE_NOMES.size(); i++) {
            if (LISTA_DE_NOMES.get(i).equals(oldName)) {
                LISTA_DE_NOMES.remove(oldName);
            }
        }
    }

    public static void main(String args[]) {
        MAP_CLIENTES = new HashMap();
        try {
            ServerSocket server = new ServerSocket(7878);
            System.out.println("Servidor rodando na porta " + server.getLocalPort());
            while (true) {
                Socket conexao = server.accept();
                Thread t = new Servidor(conexao);
                t.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void run() {
        try {
            BufferedReader entrada
                    = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            PrintStream saida = new PrintStream(this.conexao.getOutputStream());
            this.nomeCliente = entrada.readLine();
            if (armazena(this.nomeCliente)) {
                saida.println("Este nome ja existe! Conecte novamente com outro Nome.");
                this.conexao.close();
                return;
            } else {
                //mostra o nome do cliente conectado ao servidor
                System.out.println(this.nomeCliente + " : Conectado ao Servidor!");
                //Quando o cliente se conectar recebe todos que estão conectados
                saida.println("Conectados: " + LISTA_DE_NOMES.toString());
            }
            if (this.nomeCliente == null) {
                return;
            }
            //adiciona os dados de saida do cliente no objeto MAP_CLIENTES
            //A chave será o nome e valor o printstream
            MAP_CLIENTES.put(this.nomeCliente, saida);
            String[] msg = entrada.readLine().split(":");
            while (msg != null && !(msg[0].trim().equals(""))) {
                send(saida, " escreveu: ", msg);
                msg = entrada.readLine().split(":");
            }
            System.out.println(this.nomeCliente + " saiu do bate-papo!");
            String[] out = {" do bate-papo!"};
            send(saida, " saiu", out);
            remove(this.nomeCliente);
            MAP_CLIENTES.remove(this.nomeCliente);
            this.conexao.close();
        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
        }
    }

    public void send(PrintStream saida, String acao, String[] msg) throws IOException {
        Iterator it = MAP_CLIENTES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry cliente = (Map.Entry) it.next();
            PrintStream chat = (PrintStream) cliente.getValue();
            if (chat != saida) {
                if (msg.length == 1) {
                    sendToAll(acao, msg, chat);
                } else {
                    if (msg[1].equalsIgnoreCase((String) cliente.getKey())) {
                        sendToOne(acao, msg, chat, cliente);
                    }
                }
            }
        }
    }

    private void sendToAll(String acao, String[] msg, PrintStream chat) throws IOException {
        
        chat.println(this.nomeCliente + acao + msg[0]);

    }

    private void sendToOne(String acao, String[] msg, PrintStream chat, Map.Entry cliente) throws IOException {
        out:
        if (msg[1].equalsIgnoreCase((String) cliente.getKey())) {
            chat.println(this.nomeCliente + acao + msg[0]);
            break out;
        }

    }
}
