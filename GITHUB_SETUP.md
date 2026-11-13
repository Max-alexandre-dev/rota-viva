# Publicar no GitHub — sem importar histórico antigo

1. Na conta (ou organização) escolhida, crie um repositório **novo**, **público**, chamado `rota-viva`.
2. **Não** selecione README, `.gitignore` ou licença no formulário do GitHub: estes arquivos já estão no pacote.
3. Cole a descrição abaixo no campo **Description**:

> Plataforma de logística humanitária que conecta centros de apoio, identifica déficits e excedentes de recursos e propõe transferências com aprovação humana. Projeto da Equipe 404 para o Hack Brasil 2026.

4. Adicione os tópicos: `hackbrasil2026`, `logistica`, `ajuda-humanitaria`, `vuejs`, `typescript`, `spring-boot`, `java`, `postgresql`, `hackathon`.
5. **No terminal dentro da pasta `rota-viva-repositorio/`**, após extrair o ZIP:

```powershell
git init
git branch -M main
git add .
git commit -m "chore: inicia monorepo Rota Viva - Equipe 404"
git remote add origin https://github.com/SEU-USUARIO-OU-ORG/rota-viva.git
git push -u origin main
```

Altere `SEU-USUARIO-OU-ORG` para o dono real do repositório.

**Atenção ao regulamento:** repositório criado após 04/11/2026 21h (Brasília), commits após esse horário, sem forks nem histórico anterior importado. Se parte deste código tiver origem diferente ou anterior ao evento, revise a elegibilidade antes da publicação. Não versione segredos ou dados de pessoas reais. Não use um repositório antigo.

6. Convide os outros desenvolvedores em **Settings → Collaborators** (ou equipe da organização).
7. Trabalhe por branches `feature/...` e Pull Requests revisados por colega.

**Não há operação de publicação automática neste pacote.** A publicação ocorre quando você executar o push em sua conta GitHub.
