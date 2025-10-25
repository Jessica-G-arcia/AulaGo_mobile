package com.example.aulago;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

// Classe auxiliar para aplicar máscaras formatadas em tempo real
public abstract class MaskUtil {

    // Constantes das Máscaras
    private static final String MASK_CPF = "###.###.###-##";
    private static final String MASK_FONE = "(##) #####-####"; // Inclui o 9º dígito
    private static final String MASK_DATA = "##/##/####";

    // Enum para identificar o tipo de máscara
    public enum MaskType {
        CPF, FONE, DATA
    }

    // Método principal para aplicar a máscara a um EditText
    public static TextWatcher insert(final EditText editText, final MaskType maskType) {
        return new TextWatcher() {
            boolean isUpdating;
            String old = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Obtém a máscara correta
                String mask;
                String text = s.toString();

                switch (maskType) {
                    case CPF:
                        mask = MASK_CPF;
                        break;
                    case FONE:
                        // Lógica especial para telefone (com ou sem 9º dígito)
                        // Se tiver 11 dígitos, usa a máscara completa, senão, a antiga (opcional, se precisar de telefone fixo)
                        if (text.length() > 10) {
                            mask = "(##) #####-####";
                        } else {
                            mask = "(##) ####-####";
                        }
                        break;
                    case DATA:
                        mask = MASK_DATA;
                        break;
                    default:
                        mask = "";
                        break;
                }

                String str = text.replaceAll("[^0-9]*", "");
                String mascara = "";
                int i = 0;

                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }

                // Cria a máscara
                for (char m : mask.toCharArray()) {
                    if (m != '#' && str.length() > old.length()) {
                        mascara += m;
                        continue;
                    }
                    try {
                        mascara += str.charAt(i);
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }

                isUpdating = true;
                editText.setText(mascara);
                // Move o cursor para o final
                editText.setSelection(mascara.length());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }
        };
    }
}
