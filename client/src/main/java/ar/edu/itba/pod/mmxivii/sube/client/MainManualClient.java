package ar.edu.itba.pod.mmxivii.sube.client;

import ar.edu.itba.pod.mmxivii.sube.common.*;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.server.UID;
import java.util.UUID;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_CLIENT_BIND;

public class MainManualClient extends BaseMain{

    private CardClient cardClient = null;
    private Card card = null;

    private MainManualClient(@Nonnull String[] args) throws Exception {
        super(args, DEFAULT_CLIENT_OPTIONS);
        getRegistry();
        cardClient = Utils.lookupObject(CARD_CLIENT_BIND);
        card = cardClient.newCard(randomCardId(), "");
    }

    public static void main (String[] args) {
        try {
            final MainManualClient mainManualClient = new MainManualClient(args);
            mainManualClient.run();
        } catch (Exception e) {
            System.out.println("Problema al iniciar el cliente!");
            e.printStackTrace();
        }
    }

    private void run(){

        String line;
        String command = "";

        do{
            System.out.print("\nIngrese un comando: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            UID cardId = card.getId();

            try {
                line = br.readLine();
                String[] splitLine = line.split(" ");
                if(splitLine.length > 2){
                    System.out.println("Comando incorrecto!\n\n" + getHelp());
                }else{
                    command = splitLine[0];
                    int parameter;
                    double result = 0;

                    switch (command){
                        case "getBalance":
                            result = cardClient.getCardBalance(cardId);
                            break;
                        case "travel":
                            if(splitLine.length > 1){
                                parameter = Integer.parseInt(splitLine[1]);
                                result = cardClient.travel(cardId, "viaje", parameter);
                            }else{
                                System.out.println(getHelp());
                            }
                            break;
                        case "recharge":
                            if(splitLine.length > 1){
                                parameter = Integer.parseInt(splitLine[1]);
                                result = cardClient.recharge(cardId, "recarga", parameter);
                            }else{
                                System.out.println(getHelp());
                            }
                            break;
                        case "exit":
                            break;
                            //TODO: cerrar el cliente de manera feliz :P
                        default:
                            System.out.println("Comando incorrecto!\n\n" + getHelp());
                            break;
                    }

                    if(result < 0){
                        errorMessage((int)result);
                    }else if(!command.equals("exit")){
                        System.out.println("Saldo restante: " + result);
                    }
                }

            } catch (IOException ioe) {
                System.out.println("Error de IO leyendo comando!");
                System.exit(1);
            }
        }while(!command.equals("exit"));

        System.out.println("Cerrando cliente...");
    }

    private void errorMessage(int result){
        switch (result){
            case (int) CardRegistry.CARD_NOT_FOUND:
                System.out.println("ERROR: tarjeta no encontrada.");
                break;
            case (int) CardRegistry.CANNOT_PROCESS_REQUEST:
                System.out.println("ERROR: no se pudo procesar el pedido.");
                break;
            case (int) CardRegistry.COMMUNICATIONS_FAILURE:
                System.out.println("ERROR: problemas en la comunicación.");
                break;
            case (int) CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE:
                System.out.println("ERROR: operación no permitida por el balancer.");
                break;
            case (int) CardRegistry.SERVICE_TIMEOUT:
                System.out.println("ERROR: el servicio expiró.");
                break;
            default:
                break;
        }
    }

    private String getHelp(){
        StringBuilder builder = new StringBuilder("Comandos disponibles: \n");
        builder.append("\t - getBalance\n");
        builder.append("\t - travel [MONTO]\n");
        builder.append("\t - recharge [MONTO]\n");
        return builder.toString();
    }

    private String randomCardId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
