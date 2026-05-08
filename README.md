<h3>Explain PostgreSQL IntelliJ plugin</h3>
Analyzes EXPLAIN plan from PostgreSQL and related (Greenplum, Citus, TimescaleDB, Amazon RedShift).
<br><br>
Shows plan and node details and visualizations with piechart, flowchart and tilemap, also gives smart recommendations to improve query.
  <p><a href="https://explain.tensor.ru/about">Learn more</a>
  <br><br>
  Usage:
   <ul>
    <li><b>Run query and analyze plan</b><br>
      <ul>
        <li><b>IntelliJ Ultimate:</b> right-click the query in Database Console and choose "Explain Plan | Explain Analyze (Tensor)"
          <br>(Webstorm requires installation of Database Tools and SQL plugin)
          <img src="imgs/ultimate%20-%20editor%20menu.png">
          Explain Analyze results:
          <img src="imgs/explain%20analyze.png">
          Explain results:          
          <img src="imgs/explain.png">
        </li>
        <li><b>IntelliJ Community:</b> requires <a href="https://plugins.jetbrains.com/plugin/1800-database-navigator">Database Navigator (DBN)</a> plugin. Then you can either right-click the query in the editor and select "Explain Analyze (Tensor)", or use the corresponding action on the DBN toolbar.</li>
        <img src="imgs/dbn%20-%20explain%20analyze.png">
        <img src="imgs/dbn%20-%20explain.png">
        <img src="imgs/dbn%20-%20editor%20menu.png">
      </ul>
    </li>
    <li><b>Format query</b> – right-click the current query and in context menu choose "SQL Format" or press "Ctrl+Q F"
      (uses the public api from https://explain.tensor.ru , the site can be changed in Settings | Tools | Explain PostgreSQL)
      <img src="imgs/sqlformat.png">    
    </li>
    <li><b>Plan analyze manually</b> – open "Explain PostgreSQL" tool window and paste the plan from any source
      <img src="imgs/paste%20plan.png">
    </li>
   </ul>
  <br>
  <a href="https://n.sbis.ru/explain">Support</a>
