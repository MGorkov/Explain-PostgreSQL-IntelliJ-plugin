<h3>Explain PostgreSQL IntelliJ plugin</h3>
  Analyzes EXPLAIN plan from PostgreSQL and related (Greenplum, Citus, TimescaleDB and Amazon RedShift).
  <br><br>
  Shows plan and node details and visualizations with piechart, flowchart and tilemap, also gives smart recommendations to improve query.
  <p><a href="https://explain.tensor.ru/about">Learn more</a>
  <br><br>
  Usage:
   <ul>
    <li>Plan analyze - open "Explain PostgreSQL" tool window and paste the plan to site</li>
    <li>Format query - right-click the current query and in context menu choose "SQL Format" or press "Ctrl+Q F"
     <br>(uses the public api from the https://explain.tensor.ru , the site can be changed in Settings | Tools | Explain PostgreSQL)</br>
    </li>
    <li>Run query and analyze plan - right-click the query in "Query console" and in context menu choose "Explain Plan | Explain Analyze (Tensor)"
     <br>(Webstorm requires installation of Database Tools and SQL plugin)</br>
    </li>
    </ul>
  <br>
  <a href="https://n.sbis.ru/explain">Support</a>
