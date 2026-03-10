# Trab de Kotlin - Controle de Despesas 💸

Eae professor, blz? Esse aqui é meu projeto final da disciplina de Desenvolvimento de Apps com Kotlin.
A ideia do app é ajudar a galera a controlar as despesa do dia a dia pra nao chegar no fim do mes no vermelho kkkk.

## O que o app faz (Funcionalidades):
- **Login e Registro:** Dá pra criar conta com email ou usar o **botão do Google** que é bem mais rapido.
- **Lista de Despesas:** Mostra tudo que vc gastou numa lista bonitona usando RecyclerView.
- **Cadastro/Edição:** Se vc clicar no "+" adiciona uma nova, se clicar em cima de uma que já existe, dá pra mudar o valor ou a descrição (ou excluir se vc desistir do gasto).
- **Modo Escuro (Dark Mode):** Fiz um botão la em cima pra trocar o tema, pq ninguem merece tela branca no escuro ne?
- **Sincronização:** Os dados salva no celular (Room) e tbm manda pro Firebase. Se tiver sem net ele salva local e dps sincroniza.
- **Diquinha Financeira:** Toda vez que abre a home, ele le um arquivo de texto nos `assets` e mostra uma dica de finanças.

## Tecnologias que usei:
- **Linguagem:** Kotlin (obvio né kkk)
- **Arquitetura:** MVVM (ViewModel, Repository, etc - deu um trabalhinho pra entender mas foi).
- **Banco de Dados:** Room pra salvar as coisa no aparelho e Firebase Realtime Database pra nuvem.
- **Auth:** Firebase Authentication (Email e Google Sign-in).
- **UI:** Material Design, ConstraintLayout e ViewBinding pra nao ficar usando aquele monte de `findViewById`.
- **Recursos:** Leitura de arquivos RAW e Assets como pedido na rubrica.

## Como rodar o projeto:
1. Da um git clone ou baixa o zip.
2. Abre no Android Studio (o meu é o Ladybug mas acho q funciona em outros tbm).
3. Tem que configurar o `google-services.json` se for usar seu proprio Firebase, mas o meu ja ta ai (tem q ver se o SHA-1 da sua maquina ta batendo).
4. Da o Play e ja era!

---
**Aluno:** Ademar Luiz Vieira Neto
**Disciplina:** Desenvolvimento de Apps com Kotlin
**Nota esperada:** 10 né prof? kkkk valeu! ✌️
